package com.discover.app.reporting.dto;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ReportingDtos {
	private ReportingDtos() {
	}

	public record CursorPage<T>(List<T> content, int number, int size, long totalElements, int totalPages,
			boolean first, boolean last) {
	}

	public record GradeYearRow(Long gradeId, String gradeName, BigDecimal oneTime, BigDecimal yearly,
			BigDecimal halfYearly, BigDecimal quarterly, BigDecimal monthly, BigDecimal totalDue, BigDecimal collected,
			BigDecimal outstanding) {
	}

	public record StudentYearRow(Long enrollmentId, Long studentId, String studentName, String admissionNumber,
			BigDecimal oneTime, BigDecimal yearly, BigDecimal halfYearly, BigDecimal quarterly, BigDecimal monthly,
			BigDecimal totalDue, BigDecimal collected, BigDecimal outstanding) {
	}

	public record MonthlyRow(Long id, String name, String admissionNumber, BigDecimal invoiced,
			BigDecimal requestedDiscount, BigDecimal approvedDiscount, BigDecimal collected, BigDecimal outstanding) {
	}

	public record MonthlyReport(String academicYear, String gradeName, Long gradeId, String studentName,
			CursorPage<MonthlyRow> rows) {
	}

	public record StudentMonth(String month, BigDecimal invoiced, BigDecimal requestedDiscount,
			BigDecimal approvedDiscount, BigDecimal collected, BigDecimal outstanding) {
	}

	public record StudentReport(Long enrollmentId, String studentName, String admissionNumber, String academicYear,
			List<StudentMonth> months, BigDecimal yearlyInvoiced, BigDecimal yearlyRequestedDiscount,
			BigDecimal yearlyApprovedDiscount, BigDecimal yearlyCollected, BigDecimal yearlyOutstanding) {
	}

	public record LateRow(Long enrollmentId, String studentName, String admissionNumber, String gradeName,
			long latePayments) {
	}

	public record PendingRow(Long enrollmentId, String studentName, String admissionNumber, String gradeName,
			BigDecimal due, LocalDate dueDate) {
	}

	public record AdmissionRow(Long studentId, String studentName, String admissionNumber, LocalDate admissionDate,
			String gradeName) {
	}

	public record PromotionRow(Long studentId, String studentName, String admissionNumber, String fromGrade,
			String toGrade) {
	}
}