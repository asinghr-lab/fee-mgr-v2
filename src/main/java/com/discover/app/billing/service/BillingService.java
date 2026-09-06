package com.discover.app.billing.service;

import com.discover.app.billing.domain.*;
import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.repository.*;
import com.discover.app.school.domain.*;
import com.discover.app.school.repository.*;
import com.discover.app.school.service.AcademicYearService;
import com.discover.app.identity.domain.User;
import com.discover.app.identity.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillingService {
	private final InvoiceRepository invoices;
	private final DiscountRequestRepository discounts;
	private final DiscountRepository discountFacts;
	private final CancellationRequestRepository cancellations;
	private final GradeFeeStructureRepository assignments;
	private final StudentEnrollmentRepository enrollments;
	private final AcademicYearService years;
	private final UserRepository users;
	private final com.discover.app.school.repository.GradeRepository grades;

	public BillingService(InvoiceRepository i, DiscountRequestRepository d, DiscountRepository df,
			CancellationRequestRepository c, GradeFeeStructureRepository a, StudentEnrollmentRepository e,
			AcademicYearService y, UserRepository u, com.discover.app.school.repository.GradeRepository g) {
		invoices = i;
		discounts = d;
		discountFacts = df;
		cancellations = c;
		assignments = a;
		enrollments = e;
		years = y;
		users = u;
		grades = g;
	}

	@Transactional
	public int generateForAllGrades(LocalDate billingMonth, String username) {
		AcademicYear y = years.requireActive();
		validateMonth(y, billingMonth);
		int count = 0;
		for (StudentEnrollment e : enrollments.findApprovedByAcademicYear(y.getId(), EnrollmentStatus.APPROVED)) {
			if (generateForEnrollment(e, y, billingMonth))
				count++;
		}
		return count;
	}

	@Transactional
	public int generateForGrade(Long gradeId, LocalDate billingMonth, String username) {
		AcademicYear y = years.requireActive();
		validateMonth(y, billingMonth);
		int count = 0;
		for (StudentEnrollment e : enrollments.findApprovedByAcademicYearAndGrade(y.getId(), gradeId,
				EnrollmentStatus.APPROVED)) {
			if (generateForEnrollment(e, y, billingMonth))
				count++;
		}
		return count;
	}

	private boolean generateForEnrollment(StudentEnrollment enrollment, AcademicYear year, LocalDate billingMonth) {
		LocalDate effectiveDate = billingMonth;
		GradeFeeStructure gfs = assignments.findActiveForGrade(enrollment.getGrade(), effectiveDate).stream()
				.findFirst().orElseThrow(() -> new IllegalStateException("No active fee structure is assigned to grade "
						+ enrollment.getGrade().getName() + " for " + billingMonth + "."));
		FeeStructure structure = gfs.getFeeStructure();
		if (structure.getStatus() != FeeStatus.ACTIVE)
			throw new IllegalStateException("The fee structure assigned to grade " + enrollment.getGrade().getName()
					+ " is INACTIVE and cannot be used for new invoices.");
		LocalDateTime now = LocalDateTime.now();
		List<FeeStructureItem> applicable = new ArrayList<>();
		for (FeeStructureItem item : structure.getItems()) {
			if (isApplicable(item.getFrequency(), year, billingMonth, now)
					&& !alreadyInvoiced(enrollment, year, item.getFrequency(), billingMonth, now))
				applicable.add(item);
		}
		if (applicable.isEmpty())
			return false;
		String number = nextInvoiceNumber(now);
		Invoice invoice = new Invoice(number, enrollment, year, billingMonth, now);
		applicable.forEach(item -> invoice
				.addItem(new InvoiceItem(item.getFeeComponent(), item.getFrequency(), item.getAmount())));
		invoices.save(invoice);
		return true;
	}

	private boolean isApplicable(FeeFrequency f, AcademicYear y, LocalDate billingMonth, LocalDateTime now) {
		int m = (f == FeeFrequency.MONTHLY ? billingMonth.getMonthValue() : now.getMonthValue());
		int start = y.getStartDate().getMonthValue();
		int academicOffset = (m - start + 12) % 12;
		return switch (f) {
		case MONTHLY -> true;
		case ONE_TIME -> academicOffset == 0;
		case YEARLY -> academicOffset == 0;
		case HALF_YEARLY -> academicOffset == 0 || academicOffset == 6;
		case QUARTERLY -> academicOffset % 3 == 0;
		};
	}

	private boolean alreadyInvoiced(StudentEnrollment e, AcademicYear y, FeeFrequency f, LocalDate billingMonth,
			LocalDateTime now) {
		if (f == FeeFrequency.MONTHLY)
			return invoices.countMonthlyPeriod(y.getId(), InvoiceStatus.ISSUED, e.getId(), billingMonth) > 0;
		LocalDate generationMonth = now.toLocalDate().withDayOfMonth(1);
		return invoices.existsByStudentEnrollmentIdAndAcademicYearIdAndStatusAndBillingMonthAndItems_Frequency(
				e.getId(), y.getId(), InvoiceStatus.ISSUED, generationMonth, f);
	}

	private void validateMonth(AcademicYear y, LocalDate m) {
		LocalDate first = m.withDayOfMonth(1);
		if (first.isBefore(y.getStartDate().withDayOfMonth(1)) || first.isAfter(y.getEndDate().withDayOfMonth(1)))
			throw new IllegalArgumentException("Billing month must fall within the active academic year.");
	}

	private String nextInvoiceNumber(LocalDateTime dt) {
		String prefix = "INV-" + dt.getYear() + "-"
				+ dt.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH) + "-";
		long max = invoices.count();
		return prefix + String.format("%06d", max + 1);
	}

	@PreAuthorize("isAuthenticated()")
	public java.util.List<com.discover.app.school.domain.Grade> allGrades() {
		return grades.findAllByOrderByDisplayOrderAscNameAsc();
	}

	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = true)
	public Page<Invoice> searchIssued(String term, int page) {
		String t = term == null || term.isBlank() ? null : term.trim();
		return invoices.searchIssued(InvoiceStatus.ISSUED, years.requireActive().getId(), t,
				PageRequest.of(Math.max(0, page), 20));
	}

	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = true)
	public Invoice detail(Long id) {
		return invoices.findDetailedById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found."));
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional
	public DiscountRequest requestDiscount(Long invoiceId, DiscountRequestForm form, String username) {
		Invoice invoice = detail(invoiceId);
		ensureIssued(invoice);
		if (discounts.existsByInvoiceIdAndStatus(invoiceId, DiscountRequestStatus.DRAFT))
			throw new IllegalStateException("An active discount request already exists for this invoice.");
		InvoiceItem item = invoice.getItems().stream().filter(x -> x.getId().equals(form.invoiceItemId())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invoice item does not belong to this invoice."));
		if (form.requestedAmount().compareTo(item.getNetAmount()) > 0)
			throw new IllegalArgumentException("Discount cannot exceed the current item net amount.");
		User user = users.findByUsername(username).orElseThrow();
		DiscountRequest r = new DiscountRequest(invoice, user, form.reason().trim());
		r.addItem(item, form.requestedAmount(), form.reason().trim());
		return discounts.save(r);
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional
	public InvoiceCancellationRequest requestCancellation(Long invoiceId, String reason, String username) {
		Invoice invoice = detail(invoiceId);
		ensureIssued(invoice);
		if (cancellations.existsByInvoiceIdAndStatus(invoiceId, CancellationRequestStatus.DRAFT))
			throw new IllegalStateException("An active cancellation request already exists for this invoice.");
		User user = users.findByUsername(username).orElseThrow();
		return cancellations.save(new InvoiceCancellationRequest(invoice, user, reason.trim()));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void approveDiscount(Long id, String username) {
		DiscountRequest r = discounts.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Discount request not found."));
		User admin = users.findByUsername(username).orElseThrow();
		r.approve(admin);
		for (var item : r.getItems())
			discountFacts
					.save(new Discount(r.getInvoice(), item.getInvoiceItem(), r, item.getRequestedAmount(), admin));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void rejectDiscount(Long id, String username) {
		DiscountRequest r = discounts.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Discount request not found."));
		r.reject(users.findByUsername(username).orElseThrow());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void approveCancellation(Long id, String username) {
		InvoiceCancellationRequest r = cancellations.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Cancellation request not found."));
		r.approve(users.findByUsername(username).orElseThrow());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void rejectCancellation(Long id, String username) {
		InvoiceCancellationRequest r = cancellations.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Cancellation request not found."));
		r.reject(users.findByUsername(username).orElseThrow());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional(readOnly = true)
	public List<DiscountRequest> activeDiscounts() {
		return discounts.findDetailedByStatus(DiscountRequestStatus.DRAFT);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional(readOnly = true)
	public List<DiscountRequest> discountHistory() {
		return discounts.findAllDetailed();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional(readOnly = true)
	public List<InvoiceCancellationRequest> activeCancellations() {
		return cancellations.findDetailedByStatus(CancellationRequestStatus.DRAFT);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional(readOnly = true)
	public List<InvoiceCancellationRequest> cancellationHistory() {
		return cancellations.findAllDetailed();
	}

	public InvoiceResponse response(Invoice i) {
		return new InvoiceResponse(i.getId(), i.getInvoiceNumber(), i.getStudentEnrollment().getStudent().getId(),
				i.getStudentEnrollment().getStudent().getFullName(),
				i.getStudentEnrollment().getStudent().getAdmissionNumber(),
				i.getStudentEnrollment().getGrade().getName(), i.getAcademicYear().getName(), i.getBillingMonth(),
				i.getGenerationDate(), i.getStatus(), i.getOriginalAmount(), i.getDiscountAmount(), i.getNetAmount(), i
						.getItems().stream().map(x -> new InvoiceItemResponse(x.getId(), x.getFeeComponentName(),
								x.getFrequency(), x.getOriginalAmount(), x.getDiscountAmount(), x.getNetAmount()))
						.toList());
	}

	private void ensureIssued(Invoice i) {
		if (i.getStatus() != InvoiceStatus.ISSUED)
			throw new IllegalStateException("Only ISSUED invoices can be changed.");
	}
}
