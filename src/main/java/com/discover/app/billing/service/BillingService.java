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
	private final com.discover.app.school.service.SchoolService schoolService;

	public BillingService(InvoiceRepository i, DiscountRequestRepository d, DiscountRepository df,
			CancellationRequestRepository c, GradeFeeStructureRepository a, StudentEnrollmentRepository e,
			AcademicYearService y, UserRepository u, com.discover.app.school.repository.GradeRepository g,
			com.discover.app.school.service.SchoolService ss) {
		invoices = i;
		discounts = d;
		discountFacts = df;
		cancellations = c;
		assignments = a;
		enrollments = e;
		years = y;
		users = u;
		grades = g;
		schoolService = ss;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public List<InvoiceGenerationLog> generateForGrade(Long gradeId, LocalDate billingMonth, String username) {
		AcademicYear y = years.requireActive();
		billingMonth = billingMonth.withDayOfMonth(1);
		validateMonth(y, billingMonth);
		List<InvoiceGenerationLog> logs = new ArrayList<>();
		List<StudentEnrollment> candidates = enrollments.findApprovedByAcademicYearAndGrade(y.getId(), gradeId,
				EnrollmentStatus.APPROVED);
		for (StudentEnrollment e : candidates)
			logs.add(generateForEnrollment(e, y, billingMonth));
		return logs;
	}

	private InvoiceGenerationLog generateForEnrollment(StudentEnrollment enrollment, AcademicYear year,
			LocalDate billingMonth) {
		String studentName = enrollment.getStudent().getFullName();
		String admission = enrollment.getStudent().getAdmissionNumber();
		String gradeName = enrollment.getGrade().getName();
		GradeFeeStructure gfs = assignments.findForGradeId(enrollment.getGrade().getId()).stream().findFirst()
				.orElse(null);
		if (gfs == null)
		{
			System.out.print("No FeeStructure linked to Grade");
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"No FeeStructure linked to Grade", null);
		}	
		FeeStructure structure = gfs.getFeeStructure();
		if (structure.getStatus() != FeeStatus.ACTIVE)
		{
			System.out.print("Linked FeeStructure is INACTIVE");			
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"Linked FeeStructure is INACTIVE", null);
		}
		LocalDateTime now = LocalDateTime.now();
		List<InvoiceStatus> blocking = List.of(InvoiceStatus.DRAFT, InvoiceStatus.ISSUED, InvoiceStatus.PAID);
		Invoice existingMonthly = invoices
				.findBlockingMonthlyInvoice(year.getId(), enrollment.getId(), blocking, billingMonth).stream()
				.findFirst().orElse(null);
		if (existingMonthly != null) {
			System.out.println("Monthly Invoice already exists for billing month with status " + existingMonthly.getStatus() +" "+
					existingMonthly.getInvoiceNumber());
		
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"Monthly Invoice already exists for billing month with status " + existingMonthly.getStatus(),
					existingMonthly.getInvoiceNumber());
		}
		
		List<FeeStructureItem> applicable = structure.getItems().stream()
				.filter(item -> isApplicable(item.getFrequency(), year, billingMonth, now)).toList();
		if (applicable.isEmpty()) {
			System.out.print("No applicable fee components for the selected billing month");
		
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"No applicable fee components for the selected billing month", null);
		}
		String number = nextInvoiceNumber(billingMonth.atStartOfDay());
		Invoice invoice = new Invoice(number, enrollment, year, billingMonth, now,
				calculateDueDate(applicable, billingMonth, now));
		applicable.forEach(item -> invoice
				.addItem(new InvoiceItem(item.getFeeComponent(), item.getFrequency(), item.getAmount())));
		invoices.save(invoice);
		return new InvoiceGenerationLog(studentName, admission, gradeName, "GENERATED",
				"Invoice generated successfully", number);
	}
	
	
	private InvoiceGenerationLog generateForEnrollmentNonMonthly(StudentEnrollment enrollment, AcademicYear year,
			LocalDate billingMonth) {
		String studentName = enrollment.getStudent().getFullName();
		String admission = enrollment.getStudent().getAdmissionNumber();
		String gradeName = enrollment.getGrade().getName();
		GradeFeeStructure gfs = assignments.findForGradeId(enrollment.getGrade().getId()).stream().findFirst()
				.orElse(null);
		if (gfs == null)
		{
			System.out.print("No FeeStructure linked to Grade");
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"No FeeStructure linked to Grade", null);
		}	
		FeeStructure structure = gfs.getFeeStructure();
		if (structure.getStatus() != FeeStatus.ACTIVE)
		{
			System.out.print("Linked FeeStructure is INACTIVE");			
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"Linked FeeStructure is INACTIVE", null);
		}
		LocalDateTime now = LocalDateTime.now();
		List<InvoiceStatus> blocking = List.of(InvoiceStatus.DRAFT, InvoiceStatus.ISSUED, InvoiceStatus.PAID);
		Invoice existingMonthly = invoices
				.findBlockingMonthlyInvoice(year.getId(), enrollment.getId(), blocking, billingMonth).stream()
				.findFirst().orElse(null);
		if (existingMonthly != null) {
			System.out.print("Monthly Invoice already exists for billing month with status");
		
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"Monthly Invoice already exists for billing month with status " + existingMonthly.getStatus(),
					existingMonthly.getInvoiceNumber());
		}
		LocalDate generationMonthStart = now.toLocalDate().withDayOfMonth(1);
		LocalDate generationMonthEnd = generationMonthStart.plusMonths(1);
		Invoice existingGenerated = invoices.findBlockingGeneratedInMonth(year.getId(), enrollment.getId(), blocking,
				generationMonthStart.atStartOfDay(), generationMonthEnd.atStartOfDay()).stream().findFirst().orElse(null);
		if (existingGenerated != null) {
			System.out.print("Generic Invoice already generated in generation month with status");
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"Generic Invoice already generated in generation month with status " + existingGenerated.getStatus(),
					existingGenerated.getInvoiceNumber());
		}
		List<FeeStructureItem> applicable = structure.getItems().stream()
				.filter(item -> isApplicable(item.getFrequency(), year, billingMonth, now)).toList();
		if (applicable.isEmpty()) {
			System.out.print("No applicable fee components for the selected billing month");
		
			return new InvoiceGenerationLog(studentName, admission, gradeName, "SKIPPED",
					"No applicable fee components for the selected billing month", null);
		}
		String number = nextInvoiceNumber(now);
		Invoice invoice = new Invoice(number, enrollment, year, billingMonth, now,
				calculateDueDate(applicable, billingMonth, now));
		applicable.forEach(item -> invoice
				.addItem(new InvoiceItem(item.getFeeComponent(), item.getFrequency(), item.getAmount())));
		invoices.save(invoice);
		return new InvoiceGenerationLog(studentName, admission, gradeName, "GENERATED",
				"Invoice generated successfully", number);
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

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional(readOnly = true)
	public Page<InvoiceListRow> searchIssuedForView(String term, int page) {
		Page<Invoice> result = searchIssued(term, page);
		List<InvoiceListRow> rows = result.getContent().stream()
				.map(i -> new InvoiceListRow(i.getId(), i.getInvoiceNumber(),
						i.getStudentEnrollment().getStudent().getFullName(),
						i.getStudentEnrollment().getStudent().getAdmissionNumber(),
						i.getStudentEnrollment().getGrade().getName(), i.getBillingMonth(), i.getGenerationDate(),
						i.getNetAmount()))
				.toList();
		return new PageImpl<>(rows, result.getPageable(), result.getTotalElements());
	}

	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = true)
	public Invoice detail(Long id) {
		return invoices.findDetailedById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found."));
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional(readOnly = true)
	public Optional<DiscountRequest> discountRequestForInvoice(Long invoiceId) {
		return discounts.findLatestByInvoiceId(invoiceId);
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional(readOnly = true)
	public Optional<InvoiceCancellationRequest> latestCancellationRequestForInvoice(Long invoiceId) {
		return cancellations.findLatestByInvoiceId(invoiceId).stream().findFirst();
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@Transactional
	public DiscountRequest requestDiscount(Long invoiceId, DiscountRequestForm form, String username) {
		Invoice invoice = detail(invoiceId);
		ensureIssued(invoice);
		if (discounts.existsByInvoiceId(invoiceId))
			throw new IllegalStateException(
					"A discount request has already been created for this invoice. Only one discount request is allowed during the invoice lifetime.");
		InvoiceItem item = invoice.getItems().stream().filter(x -> x.getId().equals(form.invoiceItemId())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invoice item does not belong to this invoice."));
		if (form.requestedAmount().compareTo(item.getNetAmount()) > 0)
			throw new IllegalArgumentException("Discount cannot exceed the current item net amount.");
		User user = users.findByUsername(username).orElseThrow();
		DiscountRequest r = new DiscountRequest(invoice, user, form.reason().trim());
		r.addItem(item, form.requestedAmount(), form.reason().trim());
		invoice.markDraft();
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
		invoice.markDraft();
		return cancellations.save(new InvoiceCancellationRequest(invoice, user, reason.trim()));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void approveDiscount(Long id, String username) {
		DiscountRequest r = discounts.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Discount request not found."));
		User admin = users.findByUsername(username).orElseThrow();
		if (r.getInvoice().getStatus() != InvoiceStatus.DRAFT)
			throw new IllegalStateException("The invoice is not awaiting a discount decision.");
		r.approve(admin);
		r.getInvoice().issue();
		for (var item : r.getItems())
			discountFacts
					.save(new Discount(r.getInvoice(), item.getInvoiceItem(), r, item.getRequestedAmount(), admin));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void rejectDiscount(Long id, String username) {
		DiscountRequest r = discounts.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Discount request not found."));
		if (r.getInvoice().getStatus() != InvoiceStatus.DRAFT)
			throw new IllegalStateException("The invoice is not awaiting a discount decision.");
		r.reject(users.findByUsername(username).orElseThrow());
		r.getInvoice().issue();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void approveCancellation(Long id, String username) {
		InvoiceCancellationRequest r = cancellations.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Cancellation request not found."));
		if (r.getInvoice().getStatus() != InvoiceStatus.DRAFT)
			throw new IllegalStateException("The invoice is not awaiting a cancellation decision.");
		r.approve(users.findByUsername(username).orElseThrow());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void rejectCancellation(Long id, String username) {
		InvoiceCancellationRequest r = cancellations.findDetailedById(id)
				.orElseThrow(() -> new IllegalArgumentException("Cancellation request not found."));
		if (r.getInvoice().getStatus() != InvoiceStatus.DRAFT)
			throw new IllegalStateException("The invoice is not awaiting a cancellation decision.");
		r.reject(users.findByUsername(username).orElseThrow());
		r.getInvoice().issue();
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
				i.getGenerationDate(), i.getStatus(), i.getDueDate(), i.getOriginalAmount(), i.getDiscountAmount(),
				i.getNetAmount(), i
						.getItems().stream().map(x -> new InvoiceItemResponse(x.getId(), x.getFeeComponentName(),
								x.getFrequency(), x.getOriginalAmount(), x.getDiscountAmount(), x.getNetAmount()))
						.toList());
	}

	private LocalDate calculateDueDate(List<FeeStructureItem> items, LocalDate billingMonth, LocalDateTime now) {
		boolean monthly = items.stream().anyMatch(x -> x.getFrequency() == FeeFrequency.MONTHLY);
		if (monthly) {
			var school = schoolService.getSchool();
			int day = school == null ? 10 : school.getFeeDueDay();
			return billingMonth.withDayOfMonth(Math.min(day, billingMonth.lengthOfMonth()));
		}
		return now.toLocalDate();
	}

	private void ensureIssued(Invoice i) {
		if (i.getStatus() != InvoiceStatus.ISSUED)
			throw new IllegalStateException("Only ISSUED invoices can be changed.");
	}
}
