package com.discover.app.billing.dto;

import com.discover.app.billing.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class BillingDtos {
	private BillingDtos() {
	}

	public record FeeComponentResponse(Long id, String name, FeeStatus status) {
	}

	public record FeeComponentRequest(@NotBlank @Size(max = 120) String name) {
	}

	public record FeeStructureItemRequest(@NotNull Long feeComponentId, @NotNull FeeFrequency frequency,
			@NotNull @DecimalMin("0.01") BigDecimal amount) {
	}

	public record FeeStructureRequest(@NotBlank @Size(max = 120) String name,
			@NotEmpty List<@Valid FeeStructureItemRequest> items) {
	}

	public record FeeStructureItemResponse(Long id, String componentName, FeeFrequency frequency, BigDecimal amount) {
	}

	public record FeeStructureResponse(Long id, String name, FeeStatus status, List<FeeStructureItemResponse> items) {
	}

	public record GradeFeeStructureRequest(@NotNull Long gradeId, @NotNull Long feeStructureId,
			@NotNull LocalDate effectiveFrom) {
	}

	public record GradeFeeStructureResponse(Long id, Long gradeId, String gradeName, Long feeStructureId,
			String feeStructureName, LocalDate effectiveFrom, LocalDate effectiveTo) {
	}

	public record InvoiceItemResponse(Long id, String componentName, FeeFrequency frequency, BigDecimal originalAmount,
			BigDecimal discountAmount, BigDecimal netAmount) {
	}

	public record InvoiceListRow(Long id, String invoiceNumber, String studentName, String admissionNumber,
			String gradeName, LocalDate billingMonth, LocalDateTime generationDate, BigDecimal netAmount) {
	}

	public record InvoiceResponse(Long id, String invoiceNumber, Long studentId, String studentName,
			String admissionNumber, String gradeName, String academicYear, LocalDate billingMonth,
			LocalDateTime generationDate, InvoiceStatus status, LocalDate dueDate, BigDecimal originalAmount,
			BigDecimal discountAmount, BigDecimal netAmount, List<InvoiceItemResponse> items) {
	}

	public record DiscountRequestForm(@NotNull Long invoiceItemId,
			@NotNull @DecimalMin("0.01") BigDecimal requestedAmount, @NotBlank @Size(max = 500) String reason) {
	}

	public record CancellationRequestForm(@NotBlank @Size(max = 500) String reason) {
	}
}
