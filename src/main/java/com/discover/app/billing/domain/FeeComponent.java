package com.discover.app.billing.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_components", uniqueConstraints = @UniqueConstraint(name = "uk_fee_component_name", columnNames = "name"), indexes = @Index(name = "idx_fee_component_status", columnList = "status"))
public class FeeComponent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 120)
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FeeStatus status = FeeStatus.ACTIVE;
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected FeeComponent() {
	}

	public FeeComponent(String name) {
		this.name = name;
	}

	@PrePersist
	void onCreate() {
		var now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public FeeStatus getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void deactivate() {
		if (status == FeeStatus.INACTIVE)
			throw new IllegalStateException("Fee component is already inactive");
		status = FeeStatus.INACTIVE;
	}
}
