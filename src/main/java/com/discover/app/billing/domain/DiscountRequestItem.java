package com.discover.app.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "discount_request_items", indexes = @Index(name = "idx_dri_request", columnList = "discount_request_id"))
public class DiscountRequestItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "discount_request_id", nullable = false)
	private DiscountRequest discountRequest;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_item_id", nullable = false)
	private InvoiceItem invoiceItem;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal requestedAmount;
	@Column(length = 500)
	private String reason;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DiscountRequestItemStatus status = DiscountRequestItemStatus.PENDING;

	protected DiscountRequestItem() {
	}

	public DiscountRequestItem(DiscountRequest r, InvoiceItem i, BigDecimal a, String reason) {
		if (a == null || a.signum() <= 0)
			throw new IllegalArgumentException("Discount amount must be greater than zero.");
		if (a.compareTo(i.getNetAmount()) > 0)
			throw new IllegalArgumentException("Discount cannot exceed the invoice item net amount.");
		this.discountRequest = r;
		this.invoiceItem = i;
		this.requestedAmount = a;
		this.reason = reason;
	}

	public void approve() {
		status = DiscountRequestItemStatus.APPROVED;
	}

	public void reject() {
		status = DiscountRequestItemStatus.REJECTED;
	}

	public Long getId() {
		return id;
	}

	public DiscountRequest getDiscountRequest() {
		return discountRequest;
	}

	public InvoiceItem getInvoiceItem() {
		return invoiceItem;
	}

	public BigDecimal getRequestedAmount() {
		return requestedAmount;
	}

	public String getReason() {
		return reason;
	}

	public DiscountRequestItemStatus getStatus() {
		return status;
	}
}
