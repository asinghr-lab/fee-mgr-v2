package com.discover.app.reporting.web;

import com.discover.app.reporting.service.ReportingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reporting")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class ReportingWebController {
	private final ReportingService s;

	public ReportingWebController(ReportingService s) {
		this.s = s;
	}

	@GetMapping
	public String home(Model m) {
		m.addAttribute("year", s.year());
		m.addAttribute("studentCount", s.studentCountAsOf(java.time.LocalDate.now()));
		return "reporting/index";
	}

	@GetMapping("/monthly")
	public String monthly(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) Integer month,
			Model m) {
		var y = s.year();
		int ym = month == null ? y.getStartDate().getYear() * 100 + y.getStartDate().getMonthValue() : month;
		m.addAttribute("year", y);
		m.addAttribute("month", ym);
		m.addAttribute("rows", s.monthlyGrades(ym, page, 20));
		return "reporting/monthly";
	}

	@GetMapping("/yearly")
	public String yearly(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Model m) {
		m.addAttribute("year", s.year());
		m.addAttribute("rows", s.yearlyGrades(page, size));
		return "reporting/yearly";
	}

	@GetMapping("/yearly/grade/{gradeId}")
	public String grade(@PathVariable Long gradeId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Model m) {
		m.addAttribute("year", s.year());
		m.addAttribute("gradeId", gradeId);
		m.addAttribute("rows", s.yearlyStudents(gradeId, page, size));
		return "reporting/yearly-grade";
	}

	@GetMapping("/students/{id}")
	public String student(@PathVariable Long id, Model m) {
		m.addAttribute("report", s.student(id));
		return "reporting/student";
	}

	@GetMapping("/q1")
	public String q1School(Model m) {
		m.addAttribute("report", s.q1School());
		return "reporting/q1-school";
	}

	@GetMapping("/q1/grade/{gradeId}")
	public String q1Grade(@PathVariable Long gradeId, Model m) {
		m.addAttribute("report", s.q1Grade(gradeId));
		return "reporting/q1-grade";
	}

	@GetMapping("/q1/student/{enrollmentId}")
	public String q1Student(@PathVariable Long enrollmentId, Model m) {
		m.addAttribute("report", s.q1Student(enrollmentId));
		return "reporting/q1-student";
	}

	@GetMapping("/admissions/new")
	public String newAdmissions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Model m) {
		m.addAttribute("rows", s.newAdmissions(page, size));
		m.addAttribute("year", s.year());
		return "reporting/admissions";
	}

	@GetMapping("/admissions/last-month")
	public String lastMonth(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Model m) {
		m.addAttribute("rows", s.admissionsLastMonth(page, size));
		return "reporting/admissions";
	}

	@GetMapping("/promotions")
	public String promotions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Model m) {
		m.addAttribute("rows", s.promotions(page, size));
		return "reporting/promotions";
	}

	@GetMapping("/pending-fee")
	public String pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Model m) {
		m.addAttribute("rows", s.pending(page, size));
		m.addAttribute("year", s.year());
		return "reporting/pending";
	}

	@GetMapping("/late")
	public String late(@RequestParam(defaultValue = "false") boolean late, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Model m) {
		m.addAttribute("rows", s.late(late, page, size));
		m.addAttribute("late", late);
		return "reporting/late";
	}

	@GetMapping("/repeat-late")
	public String repeat(@RequestParam(defaultValue = "1") int minimum, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Model m) {
		m.addAttribute("rows", s.repeatLate(Math.max(1, minimum), page, size));
		m.addAttribute("minimum", Math.max(1, minimum));
		return "reporting/repeat-late";
	}
}