package com.discover.app.school.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class SchoolDtos {
	private SchoolDtos() {
	}

	public record SchoolRequest(@NotBlank @Size(max = 150) String name, @Size(max = 255) String address,
			@Size(max = 30) String phoneNumber, @Email @Size(max = 150) String email,
			@NotNull @Min(1) @Max(28) Integer feeDueDay) {
	}

	public record AcademicYearRequest(@NotBlank @Size(max = 20) String name, @NotNull LocalDate startDate,
			@NotNull LocalDate endDate) {
	}

	public record GradeRequest(@NotBlank @Size(max = 100) String name, @NotNull @Min(1) Integer displayOrder) {
	}

	public record StudentRequest(@NotBlank @Size(max = 50) String admissionNumber,
			@NotBlank @Size(max = 120) String firstName, @Size(max = 120) String lastName, LocalDate dateOfBirth,
			@Size(max = 20) String gender, @Size(max = 20) String phoneNumber, @Email @Size(max = 150) String email,
			@NotNull @Min(1) @Max(28) Integer feeDueDay) {
	}

	public record EnrollmentRequest(@NotNull Long studentId, @NotNull Long academicYearId, @NotNull Long gradeId) {
	}

	public record EnrollmentDecisionRequest(@NotNull Long enrollmentId) {
	}
}
