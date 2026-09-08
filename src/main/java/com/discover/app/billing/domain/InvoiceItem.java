package com.discover.app.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items", indexes = @Index(name = "idx_invoice_item_invoice", columnList = "invoice_id"))
public class InvoiceItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_component_id")
	private FeeComponent feeComponent;
	@Column(name = "fee_component_name", nullable = false, length = 120)
	private String feeComponentName;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FeeFrequency frequency;
	@Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal originalAmount;
	@Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal discountAmount = BigDecimal.ZERO;
	@Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal netAmount;

	protected InvoiceItem() {
	}

	public InvoiceItem(FeeComponent component, FeeFrequency frequency, BigDecimal originalAmount) {
		this.feeComponent = component;
		this.feeComponentName = component.getName();
		this.frequency = frequency;
		this.originalAmount = originalAmount;
		this.netAmount = originalAmount;
	}

	void attachTo(Invoice invoice) {
		this.invoice = invoice;
	}

	public void applyDiscount(BigDecimal amount) {
		if (amount == null || amount.signum() < 0)
			throw new IllegalArgumentException("Discount must not be negative.");
		BigDecimal newDiscount = discountAmount.add(amount);
		if (newDiscount.compareTo(originalAmount) > 0)
			throw new IllegalArgumentException("Discount cannot exceed the original amount.");
		discountAmount = newDiscount;
		netAmount = originalAmount.subtract(discountAmount);
	}

	public Long getId() {
		return id;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public FeeComponent getFeeComponent() {
		return feeComponent;
	}

	public String getFeeComponentName() {
		return feeComponentName;
	}

	public FeeFrequency getFrequency() {
		return frequency;
	}

	public BigDecimal getOriginalAmount() {
		return originalAmount;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}
}
