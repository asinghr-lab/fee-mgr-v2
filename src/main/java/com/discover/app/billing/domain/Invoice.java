package com.discover.app.billing.domain;

import com.discover.app.school.domain.AcademicYear;
import com.discover.app.school.domain.StudentEnrollment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(name = "uk_invoice_number", columnNames = "invoice_number"), indexes = {
		@Index(name = "idx_invoice_enrollment_period", columnList = "student_enrollment_id,academic_year_id,billing_month,status"),
		@Index(name = "idx_invoice_generation_date", columnList = "generation_date") })
public class Invoice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "invoice_number", nullable = false, length = 40)
	private String invoiceNumber;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_enrollment_id", nullable = false)
	private StudentEnrollment studentEnrollment;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYear academicYear;
	@Column(name = "billing_month", nullable = false)
	private LocalDate billingMonth;
	@Column(name = "generation_date", nullable = false)
	private LocalDateTime generationDate;
	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InvoiceStatus status = InvoiceStatus.ISSUED;
	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<InvoiceItem> items = new ArrayList<>();

	protected Invoice() {
	}

	public Invoice(String number, StudentEnrollment enrollment, AcademicYear year, LocalDate billingMonth,
			LocalDateTime generationDate, LocalDate dueDate) {
		this.invoiceNumber = number;
		this.studentEnrollment = enrollment;
		this.academicYear = year;
		this.billingMonth = billingMonth;
		this.generationDate = generationDate;
		this.dueDate = dueDate;
	}

	public void addItem(InvoiceItem item) {
		items.add(item);
		item.attachTo(this);
	}

	public void markDraft() {
		if (status != InvoiceStatus.ISSUED)
			throw new IllegalStateException("Only ISSUED invoices can be moved to DRAFT.");
		status = InvoiceStatus.DRAFT;
	}

	public void issue() {
		if (status != InvoiceStatus.DRAFT)
			throw new IllegalStateException("Only DRAFT invoices can be returned to ISSUED.");
		status = InvoiceStatus.ISSUED;
	}

	public void markPaid() {
		if (status != InvoiceStatus.ISSUED)
			throw new IllegalStateException("Only ISSUED invoices can be marked PAID.");
		status = InvoiceStatus.PAID;
	}

	public void cancel() {
		if (status != InvoiceStatus.DRAFT)
			throw new IllegalStateException("Only DRAFT invoices can be cancelled.");
		status = InvoiceStatus.CANCELLED;
	}

	public Long getId() {
		return id;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public StudentEnrollment getStudentEnrollment() {
		return studentEnrollment;
	}

	public AcademicYear getAcademicYear() {
		return academicYear;
	}

	public LocalDate getBillingMonth() {
		return billingMonth;
	}

	public LocalDateTime getGenerationDate() {
		return generationDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public InvoiceStatus getStatus() {
		return status;
	}

	public List<InvoiceItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	@Transient
	public BigDecimal getOriginalAmount() {
		return items.stream().map(InvoiceItem::getOriginalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Transient
	public BigDecimal getDiscountAmount() {
		return items.stream().map(InvoiceItem::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Transient
	public BigDecimal getNetAmount() {
		return items.stream().map(InvoiceItem::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
