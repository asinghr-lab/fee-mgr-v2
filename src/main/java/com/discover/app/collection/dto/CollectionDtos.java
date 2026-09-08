package com.discover.app.collection.dto;

import java.math.BigDecimal;
import java.time.*;

public final class CollectionDtos {
	private CollectionDtos() {
	}

	public record PaymentRequest(Long invoiceId, BigDecimal amount, LocalDateTime paidAt, String notes) {
	}

	public record PaymentResponse(Long id, String receiptNumber, Long invoiceId, String invoiceNumber,
			String studentName, String admissionNumber, BigDecimal amount, LocalDateTime paidAt, String status) {
	}
}
