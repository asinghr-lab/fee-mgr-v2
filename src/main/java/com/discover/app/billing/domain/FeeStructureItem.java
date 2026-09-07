package com.discover.app.billing.domain;

import com.discover.app.school.domain.Grade;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fee_structure_items", indexes = { @Index(name = "idx_fsi_structure", columnList = "fee_structure_id"),
		@Index(name = "idx_fsi_component", columnList = "fee_component_id") }, uniqueConstraints = @UniqueConstraint(name = "uk_fsi_structure_component", columnNames = {
				"fee_structure_id", "fee_component_id" }))
public class FeeStructureItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fee_structure_id", nullable = false)
	private FeeStructure feeStructure;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fee_component_id", nullable = false)
	private FeeComponent feeComponent;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FeeFrequency frequency;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	protected FeeStructureItem() {
	}

	public FeeStructureItem(FeeComponent component, FeeFrequency frequency, BigDecimal amount) {
		this.feeComponent = component;
		this.frequency = frequency;
		this.amount = amount;
	}

	void attachTo(FeeStructure structure) {
		this.feeStructure = structure;
	}

	public Long getId() {
		return id;
	}

	public FeeStructure getFeeStructure() {
		return feeStructure;
	}

	public FeeComponent getFeeComponent() {
		return feeComponent;
	}

	public FeeFrequency getFrequency() {
		return frequency;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
