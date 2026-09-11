package com.discover.app.reporting.service;

import com.discover.app.billing.domain.*;
import com.discover.app.collection.domain.*;
import com.discover.app.reporting.dto.ReportingDtos.*;
import com.discover.app.reporting.repository.ReportingRepository;
import com.discover.app.school.domain.*;
import com.discover.app.school.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Service
public class ReportingService {
	private final ReportingRepository repo;
	private final AcademicYearRepository years;
	private final GradeRepository grades;
	private final StudentRepository students;
	private final com.discover.app.school.service.SchoolService schoolService;

	public ReportingService(ReportingRepository r, AcademicYearRepository y, GradeRepository g, StudentRepository s,
			com.discover.app.school.service.SchoolService schoolService) {
		repo = r;
		years = y;
		grades = g;
		students = s;
		this.schoolService = schoolService;
	}

	@Transactional(readOnly = true)
	public AcademicYear year() {
		return years.findFirstByActiveTrueOrderByStartDateDesc()
				.orElseThrow(() -> new IllegalStateException("No active academic year is configured."));
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<MonthlyRow> monthlyGrades(int yearMonth, int page, int size) {
		var y = year();
		LocalDate month = LocalDate.of(yearMonth / 100, yearMonth % 100, 1);
		var inv = repo.invoices(y.getId());
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		Map<Long, MonthlyRow> map = new LinkedHashMap<>();
		for (var e : repo.enrollments(y.getId(), EnrollmentStatus.APPROVED))
			map.put(e.getGrade().getId(),
					new MonthlyRow(e.getGrade().getId(), e.getGrade().getName(), null, z(), z(), z(), z(), z()));
		Map<Long, BigDecimal[]> a = new HashMap<>();
		for (var e : map.keySet())
			a.put(e, new BigDecimal[] { z(), z(), z(), z() });
		for (var i : inv)
			if (isReportableInvoice(i) && bucket(i, month)) {
				var x = a.computeIfAbsent(i.getStudentEnrollment().getGrade().getId(),
						k -> new BigDecimal[] { z(), z(), z(), z() });
				x[0] = x[0].add(i.getOriginalAmount());
				x[2] = x[2].add(i.getDiscountAmount());
			}
		for (var r : repo.discountRequests(y.getId()))
			if (isReportableInvoice(r.getInvoice()) && bucket(r.getInvoice(), month))
				a.computeIfAbsent(r.getInvoice().getStudentEnrollment().getGrade().getId(),
						k -> new BigDecimal[] { z(), z(), z(), z() })[1] = a
								.get(r.getInvoice().getStudentEnrollment().getGrade().getId())[1]
								.add(r.getTotalRequested());
		for (var p : pays)
			if (isReportableInvoice(p.getInvoice())
					&& p.getPaidAt().toLocalDate().getYear() == month.getYear()
					&& p.getPaidAt().toLocalDate().getMonth() == month.getMonth()) {
				var x = a.computeIfAbsent(p.getInvoice().getStudentEnrollment().getGrade().getId(),
						k -> new BigDecimal[] { z(), z(), z(), z() });
				x[3] = x[3].add(p.getAmount());
			}
		List<MonthlyRow> rows = new ArrayList<>();
		for (var e : map.entrySet()) {
			var x = a.get(e.getKey());
			rows.add(new MonthlyRow(e.getKey(), e.getValue().name(), null, x[0], x[1], x[2], x[3],
					x[0].subtract(x[2]).subtract(x[3])));
		}
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<GradeYearRow> yearlyGrades(int page, int size) {
		var y = year();
		var invoices = repo.invoices(y.getId());
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		Map<Long, GradeYearRow> m = new LinkedHashMap<>();
		for (var e : repo.enrollments(y.getId(), EnrollmentStatus.APPROVED))
			m.put(e.getGrade().getId(), new GradeYearRow(e.getGrade().getId(), e.getGrade().getName(), z(), z(), z(),
					z(), z(), z(), z(), z()));
		Map<Long, BigDecimal[]> agg = new LinkedHashMap<>();
		for (var e : m.keySet())
			agg.put(e, new BigDecimal[] { z(), z(), z(), z(), z(), z() });
		for (var i : invoices) {
			if (i.getStatus() == InvoiceStatus.CANCELLED || i.getStatus() == InvoiceStatus.DRAFT)
				continue;
			var a = agg.computeIfAbsent(i.getStudentEnrollment().getGrade().getId(),
					k -> new BigDecimal[] { z(), z(), z(), z(), z(), z() });
			for (var it : i.getItems())
				a[idx(it.getFrequency())] = a[idx(it.getFrequency())].add(it.getNetAmount());
		}
		Map<Long, BigDecimal> collected = new HashMap<>();
		for (var p : pays) {
			if (isReportableInvoice(p.getInvoice()))
				collected.merge(p.getInvoice().getStudentEnrollment().getGrade().getId(), p.getAmount(),
						BigDecimal::add);
		}
		List<GradeYearRow> rows = new ArrayList<>();
		for (var e : m.entrySet()) {
			var a = agg.get(e.getKey());
			BigDecimal due = Arrays.stream(a).reduce(z(), BigDecimal::add);
			BigDecimal c = collected.getOrDefault(e.getKey(), z());
			rows.add(new GradeYearRow(e.getKey(), e.getValue().gradeName(), a[0], a[1], a[2], a[3], a[4], due, c,
					due.subtract(c)));
		}
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<StudentYearRow> yearlyStudents(Long gradeId, int page, int size) {
		var y = year();
		List<StudentYearRow> rows = new ArrayList<>();
		var inv = repo.invoices(y.getId());
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		for (var e : repo.enrollments(y.getId(), EnrollmentStatus.APPROVED)) {
			if (gradeId != null && !e.getGrade().getId().equals(gradeId))
				continue;
			BigDecimal[] a = { z(), z(), z(), z(), z() };
			for (var i : inv)
				if (i.getStudentEnrollment().getId().equals(e.getId()) && isReportableInvoice(i))
					for (var it : i.getItems())
						a[idx(it.getFrequency())] = a[idx(it.getFrequency())].add(it.getNetAmount());
			BigDecimal c = pays.stream()
					.filter(p -> p.getInvoice().getStudentEnrollment().getId().equals(e.getId())
							&& isReportableInvoice(p.getInvoice()))
					.map(Payment::getAmount).reduce(z(), BigDecimal::add);
			BigDecimal due = Arrays.stream(a).reduce(z(), BigDecimal::add);
			rows.add(new StudentYearRow(e.getId(), e.getStudent().getId(), e.getStudent().getFullName(),
					e.getStudent().getAdmissionNumber(), a[0], a[1], a[2], a[3], a[4], due, c, due.subtract(c)));
		}
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public StudentReport student(Long enrollmentId) {
		var y = year();
		var e = repo.enrollments(y.getId(), EnrollmentStatus.APPROVED).stream()
				.filter(x -> x.getId().equals(enrollmentId)).findFirst().orElseThrow();
		var inv = repo.invoices(y.getId());
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		var req = repo.discountRequests(y.getId());
		List<StudentMonth> months = new ArrayList<>();
		LocalDate m = y.getStartDate().withDayOfMonth(1);
		BigDecimal yi = z(), yr = z(), ya = z(), yc = z();
		for (int n = 0; n < 12; n++) {
			LocalDate mm = m.plusMonths(n);
			BigDecimal iv = z(), rq = z(), ap = z(), c = z();
			for (var i : inv)
				if (i.getStudentEnrollment().getId().equals(e.getId()) && isReportableInvoice(i)
						&& bucket(i, mm)) {
					iv = iv.add(i.getOriginalAmount());
				}
			for (var r : req)
				if (r.getInvoice().getStudentEnrollment().getId().equals(e.getId()) && bucket(r.getInvoice(), mm)) {
					rq = rq.add(r.getTotalRequested());
					if (r.getStatus() == DiscountRequestStatus.APPROVED)
						ap = ap.add(r.getTotalRequested());
				}
			for (var p : pays)
				if (p.getInvoice().getStudentEnrollment().getId().equals(e.getId())
						&& p.getPaidAt().toLocalDate().getYear() == mm.getYear()
						&& p.getPaidAt().toLocalDate().getMonth() == mm.getMonth())
					c = c.add(p.getAmount());
			months.add(new StudentMonth(mm.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), iv, rq, ap, c,
					iv.subtract(ap).subtract(c)));
			yi = yi.add(iv);
			yr = yr.add(rq);
			ya = ya.add(ap);
			yc = yc.add(c);
		}
		return new StudentReport(e.getId(), e.getStudent().getFullName(), e.getStudent().getAdmissionNumber(),
				y.getName(), months, yi, yr, ya, yc, yi.subtract(ya).subtract(yc));
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public Q1SchoolReport q1School() {
		var y = year();
		var invoiceData = q1InvoiceData(y);
		Map<Long, Q1GradeAccumulator> byGrade = new LinkedHashMap<>();
		for (var grade : grades.findAllByOrderByDisplayOrderAscNameAsc())
			byGrade.put(grade.getId(), new Q1GradeAccumulator(grade.getName()));
		for (var i : invoiceData.invoices()) {
			var e = i.getStudentEnrollment();
			var grade = e.getGrade();
			var acc = byGrade.computeIfAbsent(grade.getId(), id -> new Q1GradeAccumulator(grade.getName()));
			acc.add(i, invoiceData.paymentByInvoiceId().get(i.getId()));
		}
		List<Q1GradeRow> rows = new ArrayList<>();
		for (var e : byGrade.entrySet())
			rows.add(e.getValue().toRow(e.getKey()));
		Q1AmountAccumulator total = new Q1AmountAccumulator();
		rows.forEach(total::add);
		return new Q1SchoolReport(schoolName(), y.getName(), rows, total.toRow());
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public Q1GradeReport q1Grade(Long gradeId) {
		var y = year();
		var grade = grades.findById(gradeId).orElseThrow(() -> new IllegalArgumentException("Grade not found."));
		var invoiceData = q1InvoiceData(y);
		Map<Long, Q1StudentAccumulator> byStudent = new LinkedHashMap<>();
		for (var i : invoiceData.invoices()) {
			var e = i.getStudentEnrollment();
			if (!e.getGrade().getId().equals(gradeId))
				continue;
			var student = e.getStudent();
			var acc = byStudent.computeIfAbsent(e.getId(), id -> new Q1StudentAccumulator(
					student.getId(), student.getFullName(), student.getAdmissionNumber()));
			acc.add(i, invoiceData.paymentByInvoiceId().get(i.getId()));
		}
		List<Q1StudentRow> rows = new ArrayList<>();
		for (var e : byStudent.entrySet())
			rows.add(e.getValue().toRow(e.getKey()));
		Q1AmountAccumulator total = new Q1AmountAccumulator();
		rows.forEach(total::add);
		return new Q1GradeReport(grade.getName(), y.getName(), rows, total.toRow());
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public Q1StudentReport q1Student(Long enrollmentId) {
		var y = year();
		var enrollment = repo.enrollments(y.getId(), EnrollmentStatus.APPROVED).stream()
				.filter(e -> e.getId().equals(enrollmentId)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Student enrollment not found."));
		var invoiceData = q1InvoiceData(y);
		Q1StudentAccumulator acc = new Q1StudentAccumulator(enrollment.getStudent().getId(),
				enrollment.getStudent().getFullName(), enrollment.getStudent().getAdmissionNumber());
		for (var i : invoiceData.invoices()) {
			if (i.getStudentEnrollment().getId().equals(enrollmentId))
				acc.add(i, invoiceData.paymentByInvoiceId().get(i.getId()));
		}
		return new Q1StudentReport(enrollment.getStudent().getFullName(), enrollment.getStudent().getAdmissionNumber(),
				enrollment.getGrade().getId(), enrollment.getGrade().getName(), y.getName(), acc.toAmountRow());
	}

	private Q1InvoiceData q1InvoiceData(AcademicYear y) {
		var start = LocalDate.of(y.getStartDate().getYear(), 4, 1);
		var april = start;
		var may = start.plusMonths(1);
		var june = start.plusMonths(2);
		var invoices = repo.invoices(y.getId()).stream()
				.filter(i -> i.getStudentEnrollment().getStatus() == EnrollmentStatus.APPROVED)
				.filter(this::isQ1Invoice)
				.filter(i -> {
					LocalDate bucket = invoiceMonth(i);
					return bucket.equals(april) || bucket.equals(may) || bucket.equals(june);
				})
				.toList();
		Map<Long, BigDecimal> payments = new HashMap<>();
		for (var p : repo.payments(y.getId(), PaymentStatus.RECORDED)) {
			var invoice = p.getInvoice();
			if (invoice.getStatus() == InvoiceStatus.PAID && isQ1Invoice(invoice))
				payments.put(invoice.getId(), p.getAmount());
		}
		return new Q1InvoiceData(invoices, payments);
	}

	private boolean isQ1Invoice(Invoice i) {
		return i.getStatus() == InvoiceStatus.ISSUED || i.getStatus() == InvoiceStatus.PAID;
	}

	private LocalDate invoiceMonth(Invoice i) {
		return i.getBillingMonth().withDayOfMonth(1);
	}

	private String schoolName() {
		var school = schoolService.getSchool();
		return school == null ? "" : school.getName();
	}

	private record Q1InvoiceData(List<Invoice> invoices, Map<Long, BigDecimal> paymentByInvoiceId) {
	}

	private static final class Q1GradeAccumulator {
		private final String name;
		private final Q1AmountAccumulator amounts = new Q1AmountAccumulator();

		private Q1GradeAccumulator(String name) { this.name = name; }

		private void add(Invoice invoice, BigDecimal payment) { amounts.add(invoice, payment); }

		private Q1GradeRow toRow(Long id) {
			var a = amounts.toRow();
			return new Q1GradeRow(id, name, a.aprilCollected(), a.aprilOutstanding(), a.mayCollected(),
					a.mayOutstanding(), a.juneCollected(), a.juneOutstanding(), a.q1TotalOutstanding());
		}
	}

	private static final class Q1StudentAccumulator {
		private final Long studentId;
		private final String name;
		private final String admissionNumber;
		private final Q1AmountAccumulator amounts = new Q1AmountAccumulator();

		private Q1StudentAccumulator(Long studentId, String name, String admissionNumber) {
			this.studentId = studentId; this.name = name; this.admissionNumber = admissionNumber;
		}

		private void add(Invoice invoice, BigDecimal payment) { amounts.add(invoice, payment); }

		private Q1StudentRow toRow(Long enrollmentId) {
			var a = amounts.toRow();
			return new Q1StudentRow(enrollmentId, studentId, name, admissionNumber, a.aprilCollected(),
					a.aprilOutstanding(), a.mayCollected(), a.mayOutstanding(), a.juneCollected(), a.juneOutstanding(),
					a.q1TotalOutstanding());
		}

		private Q1AmountRow toAmountRow() { return amounts.toRow(); }
	}

	private static final class Q1AmountAccumulator {
		private BigDecimal aprilCollected = BigDecimal.ZERO, aprilOutstanding = BigDecimal.ZERO;
		private BigDecimal mayCollected = BigDecimal.ZERO, mayOutstanding = BigDecimal.ZERO;
		private BigDecimal juneCollected = BigDecimal.ZERO, juneOutstanding = BigDecimal.ZERO;

		private void add(Invoice invoice, BigDecimal payment) {
			BigDecimal collected = invoice.getStatus() == InvoiceStatus.PAID && payment != null ? payment : BigDecimal.ZERO;
			BigDecimal outstanding = invoice.getNetAmount().subtract(collected);
			LocalDate month = invoice.getBillingMonth().withDayOfMonth(1);
			if (month.getMonthValue() == 4) { aprilCollected = aprilCollected.add(collected); aprilOutstanding = aprilOutstanding.add(outstanding); }
			else if (month.getMonthValue() == 5) { mayCollected = mayCollected.add(collected); mayOutstanding = mayOutstanding.add(outstanding); }
			else if (month.getMonthValue() == 6) { juneCollected = juneCollected.add(collected); juneOutstanding = juneOutstanding.add(outstanding); }
		}

		private void add(Q1GradeRow row) {
			aprilCollected = aprilCollected.add(row.aprilCollected()); aprilOutstanding = aprilOutstanding.add(row.aprilOutstanding());
			mayCollected = mayCollected.add(row.mayCollected()); mayOutstanding = mayOutstanding.add(row.mayOutstanding());
			juneCollected = juneCollected.add(row.juneCollected()); juneOutstanding = juneOutstanding.add(row.juneOutstanding());
		}

		private void add(Q1StudentRow row) {
			aprilCollected = aprilCollected.add(row.aprilCollected()); aprilOutstanding = aprilOutstanding.add(row.aprilOutstanding());
			mayCollected = mayCollected.add(row.mayCollected()); mayOutstanding = mayOutstanding.add(row.mayOutstanding());
			juneCollected = juneCollected.add(row.juneCollected()); juneOutstanding = juneOutstanding.add(row.juneOutstanding());
		}

		private Q1AmountRow toRow() {
			return new Q1AmountRow(aprilCollected, aprilOutstanding, mayCollected, mayOutstanding, juneCollected, juneOutstanding,
					aprilOutstanding.add(mayOutstanding).add(juneOutstanding));
		}
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<PendingRow> pending(int page, int size) {
		var y = year();
		List<PendingRow> rows = new ArrayList<>();
		for (var e : repo.enrollments(y.getId(), EnrollmentStatus.APPROVED)) {
			BigDecimal due = z();
			LocalDate latest = null;
			var paidIds = repo.payments(y.getId(), PaymentStatus.RECORDED).stream().map(p -> p.getInvoice().getId())
					.collect(java.util.stream.Collectors.toSet());
			for (var i : repo.invoices(y.getId()))
				if (i.getStudentEnrollment().getId().equals(e.getId()) && i.getStatus() == InvoiceStatus.ISSUED
						&& !paidIds.contains(i.getId()) && i.getDueDate() != null
						&& !i.getDueDate().isAfter(LocalDate.now())) {
					due = due.add(i.getNetAmount());
					latest = i.getDueDate();
				}
			if (due.signum() > 0)
				rows.add(new PendingRow(e.getId(), e.getStudent().getFullName(), e.getStudent().getAdmissionNumber(),
						e.getGrade().getName(), due, latest));
		}
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<LateRow> late(boolean late, int page, int size) {
		var y = year();
		var inv = repo.invoices(y.getId());
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		Map<Long, Long> counts = new HashMap<>();
		for (var p : pays)
			if (isReportableInvoice(p.getInvoice())
					&& p.getPaidAt().toLocalDate().isAfter(p.getInvoice().getDueDate()))
				counts.merge(p.getInvoice().getStudentEnrollment().getId(), 1L, Long::sum);
		List<LateRow> rows = new ArrayList<>();
		for (var e : repo.enrollments(y.getId(), EnrollmentStatus.APPROVED)) {
			long c = counts.getOrDefault(e.getId(), 0L);
			long paid = pays.stream().filter(x -> x.getInvoice().getStudentEnrollment().getId().equals(e.getId())
					&& isReportableInvoice(x.getInvoice())).count();
			if ((late && c > 0) || (!late && paid > 0 && c == 0))
				rows.add(new LateRow(e.getId(), e.getStudent().getFullName(), e.getStudent().getAdmissionNumber(),
						e.getGrade().getName(), c));
		}
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public CursorPage<LateRow> repeatLate(int minimum, int page, int size) {
		return lateAtLeast(minimum, page, size);
	}

	private CursorPage<LateRow> lateAtLeast(int min, int page, int size) {
		var y = year();
		var pays = repo.payments(y.getId(), PaymentStatus.RECORDED);
		Map<Long, Long> counts = new HashMap<>();
		for (var p : pays)
			if (isReportableInvoice(p.getInvoice())
					&& p.getPaidAt().toLocalDate().isAfter(p.getInvoice().getDueDate()))
				counts.merge(p.getInvoice().getStudentEnrollment().getId(), 1L, Long::sum);
		List<LateRow> rows = repo.enrollments(y.getId(), EnrollmentStatus.APPROVED).stream()
				.filter(e -> counts.getOrDefault(e.getId(), 0L) >= min)
				.map(e -> new LateRow(e.getId(), e.getStudent().getFullName(), e.getStudent().getAdmissionNumber(),
						e.getGrade().getName(), counts.get(e.getId())))
				.toList();
		return page(rows, page, 20);
	}

	@Transactional(readOnly = true)
	public long studentCountAsOf(LocalDate date) {
		var y = year();
		return repo.enrollments(y.getId(), EnrollmentStatus.APPROVED).stream()
				.filter(e -> !e.getStudent().getAdmissionDate().isAfter(date) && e.getApprovalDate() != null
						&& !e.getApprovalDate().toLocalDate().isAfter(date))
				.count();
	}

	@Transactional(readOnly = true)
	public CursorPage<AdmissionRow> admissionsLastMonth(int page, int size) {
		LocalDate first = LocalDate.now().withDayOfMonth(1).minusMonths(1), last = first.plusMonths(1);
		List<AdmissionRow> rows = repo.enrollments(year().getId(), EnrollmentStatus.APPROVED).stream()
				.filter(e -> !e.getStudent().getAdmissionDate().isBefore(first)
						&& e.getStudent().getAdmissionDate().isBefore(last))
				.map(e -> new AdmissionRow(e.getStudent().getId(), e.getStudent().getFullName(),
						e.getStudent().getAdmissionNumber(), e.getStudent().getAdmissionDate(), e.getGrade().getName()))
				.toList();
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	public CursorPage<AdmissionRow> newAdmissions(int page, int size) {
		var y = year();
		List<AdmissionRow> rows = repo.enrollments(y.getId(), EnrollmentStatus.APPROVED).stream()
				.filter(e -> !e.getStudent().getAdmissionDate().isBefore(y.getStartDate())
						&& !e.getStudent().getAdmissionDate().isAfter(y.getEndDate()))
				.map(e -> new AdmissionRow(e.getStudent().getId(), e.getStudent().getFullName(),
						e.getStudent().getAdmissionNumber(), e.getStudent().getAdmissionDate(), e.getGrade().getName()))
				.toList();
		return page(rows, page, size);
	}

	@Transactional(readOnly = true)
	public CursorPage<PromotionRow> promotions(int page, int size) {
		var all = years.findAllByOrderByStartDateDesc();
		var current = year();
		var previous = all.stream().filter(x -> x.getEndDate().isBefore(current.getStartDate())).findFirst()
				.orElse(null);
		if (previous == null)
			return page(List.of(), page, size);
		var prev = repo.enrollments(previous.getId(), EnrollmentStatus.APPROVED);
		var cur = repo.enrollments(current.getId(), EnrollmentStatus.APPROVED);
		Map<Long, StudentEnrollment> pm = prev.stream()
				.collect(Collectors.toMap(e -> e.getStudent().getId(), Function.identity(), (a, b) -> a));
		List<PromotionRow> rows = cur.stream()
				.filter(e -> pm.containsKey(e.getStudent().getId())
						&& e.getGrade().getDisplayOrder() > pm.get(e.getStudent().getId()).getGrade().getDisplayOrder())
				.map(e -> new PromotionRow(e.getStudent().getId(), e.getStudent().getFullName(),
						e.getStudent().getAdmissionNumber(), pm.get(e.getStudent().getId()).getGrade().getName(),
						e.getGrade().getName()))
				.toList();
		return page(rows, page, size);
	}

	private boolean isReportableInvoice(Invoice i) {
		return i.getStatus() == InvoiceStatus.ISSUED || i.getStatus() == InvoiceStatus.PAID;
	}

	private boolean bucket(Invoice i, LocalDate m) {
		LocalDate b = i.getItems().stream().anyMatch(x -> x.getFrequency() == FeeFrequency.MONTHLY)
				? i.getBillingMonth().withDayOfMonth(1)
				: i.getGenerationDate().toLocalDate().withDayOfMonth(1);
		return b.equals(m);
	}

	private int idx(FeeFrequency f) {
		return switch (f) {
		case ONE_TIME -> 0;
		case YEARLY -> 1;
		case HALF_YEARLY -> 2;
		case QUARTERLY -> 3;
		case MONTHLY -> 4;
		};
	}

	private BigDecimal z() {
		return BigDecimal.ZERO;
	}

	private <T> CursorPage<T> page(List<T> list, int p, int s) {
		int size = Math.max(1, s), pg = Math.max(0, p), from = Math.min(pg * size, list.size()),
				to = Math.min(from + size, list.size());
		return new CursorPage<>(list.subList(from, to), pg, size, list.size(),
				(int) Math.ceil(list.size() / (double) size), pg == 0, to >= list.size());
	}
}
