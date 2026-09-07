package com.discover.app.billing.domain;

import com.discover.app.identity.domain.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts", indexes = { @Index(name = "idx_discount_invoice", columnList = "invoice_id"),
		@Index(name = "idx_discount_approved", columnList = "approved_at") })
public class Discount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_item_id", nullable = false)
	private InvoiceItem invoiceItem;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "discount_request_id", nullable = false)
	private DiscountRequest discountRequest;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "approved_by_user_id", nullable = false)
	private User approvedBy;
	@Column(name = "approved_at", nullable = false)
	private LocalDateTime approvedAt;

	protected Discount() {
	}

	public Discount(Invoice invoice, InvoiceItem item, DiscountRequest request, BigDecimal amount, User approvedBy) {
		this.invoice = invoice;
		this.invoiceItem = item;
		this.discountRequest = request;
		this.amount = amount;
		this.approvedBy = approvedBy;
		this.approvedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public InvoiceItem getInvoiceItem() {
		return invoiceItem;
	}

	public DiscountRequest getDiscountRequest() {
		return discountRequest;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public User getApprovedBy() {
		return approvedBy;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}
}
