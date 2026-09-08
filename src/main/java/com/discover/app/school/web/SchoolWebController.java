package com.discover.app.school.web;

import com.discover.app.school.dto.SchoolDtos.*;
import com.discover.app.school.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/school")
public class SchoolWebController {
	private final SchoolService school;
	private final AcademicYearService years;
	private final GradeService grades;
	private final StudentService students;
	private final EnrollmentService enrollments;

	public SchoolWebController(SchoolService school, AcademicYearService years, GradeService grades,
			StudentService students, EnrollmentService enrollments) {
		this.school = school;
		this.years = years;
		this.grades = grades;
		this.students = students;
		this.enrollments = enrollments;
	}

	@GetMapping
	public String index(Model model) {
		model.addAttribute("school", school.getSchool());
		model.addAttribute("years", years.findAll());
		model.addAttribute("grades", grades.findAll());
		return "school/index";
	}

	@GetMapping("/edit")
	@PreAuthorize("hasRole('ADMIN')")
	public String editSchool(Model model) {
		var s = school.getSchool();
		model.addAttribute("schoolRequest",
				new SchoolRequest(s == null ? "" : s.getName(), s == null ? "" : s.getAddress(),
						s == null ? "" : s.getPhoneNumber(), s == null ? "" : s.getEmail(),
						s == null ? 10 : s.getFeeDueDay()));
		model.addAttribute("pageTitle", s == null ? "Create School" : "Edit School");
		return "school/school-form";
	}

	@PostMapping("/edit")
	@PreAuthorize("hasRole('ADMIN')")
	public String saveSchool(@Valid @ModelAttribute("schoolRequest") SchoolRequest request, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "school/school-form";
		school.saveOrUpdate(request.name(), request.address(), request.phoneNumber(), request.email(),
				request.feeDueDay());
		redirect.addFlashAttribute("message", "School details saved successfully.");
		return "redirect:/school";
	}

	@GetMapping("/academic-years/new")
	@PreAuthorize("hasRole('ADMIN')")
	public String academicYearForm(Model model) {
		model.addAttribute("request", new AcademicYearRequest("", null, null));
		return "school/academic-year-form";
	}

	@PostMapping("/academic-years/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public String activateAcademicYear(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			years.activate(id);
			redirect.addFlashAttribute("message",
					"Academic year activated. All other academic years are now inactive.");
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school";
	}

	@PostMapping("/academic-years")
	@PreAuthorize("hasRole('ADMIN')")
	public String createAcademicYear(@Valid @ModelAttribute("request") AcademicYearRequest request,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "school/academic-year-form";
		try {
			years.create(request.name(), request.startDate(), request.endDate());
			redirect.addFlashAttribute("message", "Academic year created successfully.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school";
	}

	@GetMapping("/grades/new")
	@PreAuthorize("hasRole('ADMIN')")
	public String gradeForm(Model model) {
		model.addAttribute("request", new GradeRequest("", 1));
		model.addAttribute("pageTitle", "New Grade");
		return "school/grade-form";
	}

	@PostMapping("/grades")
	@PreAuthorize("hasRole('ADMIN')")
	public String createGrade(@Valid @ModelAttribute("request") GradeRequest request, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "school/grade-form";
		try {
			grades.create(request.name(), request.displayOrder());
			redirect.addFlashAttribute("message", "Grade created successfully.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school";
	}

	@GetMapping("/grades/{id}/edit")
	@PreAuthorize("hasRole('ADMIN')")
	public String editGrade(@PathVariable Long id, Model model) {
		var grade = grades.findById(id);
		model.addAttribute("gradeId", grade.getId());
		model.addAttribute("request", new GradeRequest(grade.getName(), grade.getDisplayOrder()));
		model.addAttribute("pageTitle", "Edit Grade");
		return "school/grade-form";
	}

	@PostMapping("/grades/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String updateGrade(@PathVariable Long id, @Valid @ModelAttribute("request") GradeRequest request,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "school/grade-form";
		try {
			grades.update(id, request.name(), request.displayOrder());
			redirect.addFlashAttribute("message", "Grade updated successfully.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school";
	}

	@GetMapping("/students")
	public String studentList(Model model) {
		model.addAttribute("students", students.findAll());
		model.addAttribute("activeEnrollmentStudentIds", enrollments.activeEnrollmentStudentIds());
		return "school/students";
	}

	@GetMapping("/enrollments")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String enrollmentList(@RequestParam(required = false) Long gradeId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
		int safePage = Math.max(0, page);
		int safeSize = Math.min(Math.max(size, 5), 50);
		Page<?> enrollmentPage = enrollments.findAllByGrade(gradeId, PageRequest.of(safePage, safeSize));
		model.addAttribute("enrollmentPage", enrollmentPage);
		model.addAttribute("grades", grades.findAll());
		model.addAttribute("selectedGradeId", gradeId);
		return "school/enrollments";
	}

	@GetMapping("/students/new")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String studentForm(Model model) {
		model.addAttribute("request", new StudentRequest("", "", "", null, "", "", "", null));
		return "school/student-form";
	}

	@PostMapping("/students")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String createStudent(@Valid @ModelAttribute("request") StudentRequest request, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "school/student-form";
		try {
			students.create(request.admissionNumber(), request.firstName(), request.lastName(), request.dateOfBirth(),
					request.gender(), request.phoneNumber(), request.email());
			redirect.addFlashAttribute("message", "Student added successfully. Admission date was set to today.");
		} catch (IllegalArgumentException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school/students";
	}

	@GetMapping("/students/{studentId}/enroll")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String enrollmentForm(@PathVariable Long studentId, Model model) {
		var student = students.findById(studentId);
		model.addAttribute("student", student);
		model.addAttribute("years", years.findAll());
		model.addAttribute("grades", grades.findAll());
		model.addAttribute("request", new EnrollmentRequest(studentId, null, null));
		return "school/enrollment-form";
	}

	@PostMapping("/enrollments")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String requestEnrollment(@Valid @ModelAttribute("request") EnrollmentRequest request,
			BindingResult bindingResult, Authentication authentication, Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("student", students.findById(request.studentId()));
			model.addAttribute("years", years.findAll());
			model.addAttribute("grades", grades.findAll());
			return "school/enrollment-form";
		}
		try {
			enrollments.request(request.studentId(), request.academicYearId(), request.gradeId(),
					authentication.getName());
			redirect.addFlashAttribute("message", "Enrollment request submitted for approval.");
			return "redirect:/school/students";
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
			return "redirect:/school/students/" + request.studentId() + "/enroll";
		}
	}

	@GetMapping("/enrollments/requests")
	@PreAuthorize("hasRole('ADMIN')")
	public String activeEnrollmentRequests(Model model) {
		model.addAttribute("requests", enrollments.activeRequests());
		model.addAttribute("historical", false);
		return "approval/enrollment-requests";
	}

	@GetMapping("/enrollments/history")
	@PreAuthorize("hasRole('ADMIN')")
	public String historicalEnrollmentRequests(Model model) {
		model.addAttribute("requests", enrollments.historicalRequests());
		model.addAttribute("historical", true);
		return "approval/enrollment-requests";
	}

	@PostMapping("/enrollments/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public String approveEnrollment(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
		try {
			enrollments.approve(id, authentication.getName());
			redirect.addFlashAttribute("message", "Enrollment request approved.");
		} catch (IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school/enrollments/requests";
	}

	@PostMapping("/enrollments/{id}/cancel")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String cancelEnrollment(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			enrollments.cancel(id);
			redirect.addFlashAttribute("message",
					"Enrollment request cancelled. A new request can be created if needed.");
		} catch (IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/school/enrollments/requests";
	}
}
