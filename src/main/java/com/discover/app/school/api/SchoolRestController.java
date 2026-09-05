package com.discover.app.school.api;

import com.discover.app.school.dto.SchoolDtos.*;
import com.discover.app.school.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school")
public class SchoolRestController {
	private final SchoolService school;
	private final AcademicYearService years;
	private final GradeService grades;
	private final StudentService students;
	private final EnrollmentService enrollments;

	public SchoolRestController(SchoolService school, AcademicYearService years, GradeService grades,
			StudentService students, EnrollmentService enrollments) {
		this.school = school;
		this.years = years;
		this.grades = grades;
		this.students = students;
		this.enrollments = enrollments;
	}

	@GetMapping
	public Object getSchool() {
		return school.getSchool();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public Object saveSchool(@Valid @RequestBody SchoolRequest r) {
		return school.saveOrUpdate(r.name(), r.address(), r.phoneNumber(), r.email());
	}

	@GetMapping("/academic-years")
	public Object years() {
		return years.findAll();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/academic-years")
	public Object createYear(@Valid @RequestBody AcademicYearRequest r) {
		return years.create(r.name(), r.startDate(), r.endDate());
	}

	@GetMapping("/grades")
	public Object grades() {
		return grades.findAll();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/grades")
	public Object createGrade(@Valid @RequestBody GradeRequest r) {
		return grades.create(r.name(), r.section(), r.description(), r.displayOrder());
	}

	@GetMapping("/students")
	public Object students() {
		return students.findAll();
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@PostMapping("/students")
	public Object createStudent(@Valid @RequestBody StudentRequest r) {
		return students.create(r.admissionNumber(), r.firstName(), r.lastName(), r.dateOfBirth(), r.gender(),
				r.phoneNumber(), r.email());
	}

	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	@PostMapping("/enrollments")
	public Object requestEnrollment(@Valid @RequestBody EnrollmentRequest r, Authentication a) {
		return enrollments.request(r.studentId(), r.academicYearId(), r.gradeId(), a.getName());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/enrollments/{id}/approve")
	public Object approve(@PathVariable Long id, Authentication a) {
		return enrollments.approve(id, a.getName());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/enrollments/{id}/cancel")
	public Object cancel(@PathVariable Long id) {
		return enrollments.cancel(id);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/enrollments/requests")
	public Object active() {
		return enrollments.activeRequests();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/enrollments/history")
	public Object history() {
		return enrollments.historicalRequests();
	}
}
