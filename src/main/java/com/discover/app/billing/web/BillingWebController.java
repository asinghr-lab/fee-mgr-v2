package com.discover.app.billing.web;

import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.service.BillingService;
import com.discover.app.billing.service.FeeConfigurationService;
import com.discover.app.school.service.AcademicYearService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/billing")
public class BillingWebController {
	private final BillingService service;
	private final AcademicYearService years;

	private final FeeConfigurationService feeService;

	public BillingWebController(BillingService service, AcademicYearService years, FeeConfigurationService feeService) {
		this.service = service;
		this.years = years;
		this.feeService = feeService;
	}

	@GetMapping("/fee-components")
	public String components(Model model) {
		model.addAttribute("components", feeService.allComponents());
		return "billing/fee-components";
	}

	@GetMapping("/fee-components/new")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String componentForm(Model model) {
		model.addAttribute("request", new FeeComponentRequest(""));
		return "billing/fee-component-form";
	}

	@PostMapping("/fee-components")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String createComponent(@Valid @ModelAttribute("request") FeeComponentRequest request, BindingResult result,
			RedirectAttributes redirect) {
		if (result.hasErrors())
			return "billing/fee-component-form";
		try {
			feeService.createComponent(request.name());
			redirect.addFlashAttribute("message", "Fee component created successfully.");
		} catch (IllegalArgumentException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/fee-components";
	}

	@PostMapping("/fee-components/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public String deactivateComponent(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			feeService.deactivateComponent(id);
			redirect.addFlashAttribute("message", "Fee component marked INACTIVE.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/fee-components";
	}

	@GetMapping("/fee-structures")
	public String structures(Model model) {
		model.addAttribute("structures", feeService.allStructures());
		return "billing/fee-structures";
	}

	@GetMapping("/fee-structures/new")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String structureForm(Model model) {
		model.addAttribute("components", feeService.activeComponents());
		if (!model.containsAttribute("request"))
			model.addAttribute("request", new FeeStructureRequest("",
					new ArrayList<>(List.of(new FeeStructureItemRequest(null, null, null)))));
		return "billing/fee-structure-form";
	}

	@PostMapping("/fee-structures")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String createStructure(@Valid @ModelAttribute("request") FeeStructureRequest request, BindingResult result,
			Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			model.addAttribute("components", feeService.activeComponents());
			return "billing/fee-structure-form";
		}
		try {
			feeService.createStructure(request.name(), request.items());
			redirect.addFlashAttribute("message", "Fee structure created successfully and is immutable.");
			return "redirect:/billing/fee-structures";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("components", feeService.activeComponents());
			model.addAttribute("error", ex.getMessage());
			return "billing/fee-structure-form";
		}
	}

	@PostMapping("/fee-structures/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public String deactivateStructure(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			feeService.deactivateStructure(id);
			redirect.addFlashAttribute("message",
					"Fee structure marked INACTIVE. Existing grade assignments remain intact.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/fee-structures";
	}

	@GetMapping("/grade-fee-structures")
	public String assignments(Model model) {
		model.addAttribute("assignments", feeService.assignments());
		return "billing/grade-fee-structures";
	}

	@GetMapping("/grade-fee-structures/new")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String assignmentForm(Model model) {
		model.addAttribute("grades", feeService.allGrades());
		model.addAttribute("structures", feeService.activeStructures());
		model.addAttribute("request", new GradeFeeStructureRequest(null, null, java.time.LocalDate.now()));
		return "billing/grade-fee-structure-form";
	}

	@PostMapping("/grade-fee-structures")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String assign(@Valid @ModelAttribute("request") GradeFeeStructureRequest request, BindingResult result,
			Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			model.addAttribute("grades", feeService.allGrades());
			model.addAttribute("structures", feeService.activeStructures());
			return "billing/grade-fee-structure-form";
		}
		try {
			feeService.assignToGrade(request.gradeId(), request.feeStructureId(), request.effectiveFrom());
			redirect.addFlashAttribute("message", "Fee structure assigned to grade successfully.");
			return "redirect:/billing/grade-fee-structures";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("grades", feeService.allGrades());
			model.addAttribute("structures", feeService.activeStructures());
			model.addAttribute("error", ex.getMessage());
			return "billing/grade-fee-structure-form";
		}
	}

	@GetMapping("/grade-fee-structures/{gradeId}/history")
	public String assignmentHistory(@PathVariable Long gradeId, Model model) {
		model.addAttribute("assignments", feeService.assignmentHistory(gradeId));
		model.addAttribute("gradeId", gradeId);
		return "billing/grade-fee-structure-history";
	}

	@GetMapping("/invoices")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String invoices(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
			Model model) {
		Page<InvoiceListRow> result = service.searchIssuedForView(q, page);
		model.addAttribute("page", result);
		model.addAttribute("query", q == null ? "" : q);
		model.addAttribute("activeYear", years.getActive());
		return "billing/invoices";
	}

	@GetMapping("/invoices/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String invoiceDetail(@PathVariable Long id, Model model) {
		model.addAttribute("invoice", service.detail(id));
		model.addAttribute("discountRequest", new DiscountRequestForm(null, null, ""));
		model.addAttribute("cancellationRequest", new CancellationRequestForm(""));
		return "billing/invoice-detail";
	}

	@PostMapping("/invoices/{id}/discount-requests")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String requestDiscount(@PathVariable Long id,
			@Valid @ModelAttribute("discountRequest") DiscountRequestForm form, BindingResult result,
			Authentication auth, Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			model.addAttribute("invoice", service.detail(id));
			model.addAttribute("cancellationRequest", new CancellationRequestForm(""));
			return "billing/invoice-detail";
		}
		try {
			service.requestDiscount(id, form, auth.getName());
			redirect.addFlashAttribute("message", "Discount request submitted for Admin approval.");
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/invoices/" + id;
	}

	@PostMapping("/invoices/{id}/cancellation-requests")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String requestCancellation(@PathVariable Long id,
			@Valid @ModelAttribute("cancellationRequest") CancellationRequestForm form, BindingResult result,
			Authentication auth, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			redirect.addFlashAttribute("error", "Cancellation reason is required.");
			return "redirect:/billing/invoices/" + id;
		}
		try {
			service.requestCancellation(id, form.reason(), auth.getName());
			redirect.addFlashAttribute("message", "Cancellation request submitted for Admin approval.");
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/invoices/" + id;
	}

	@GetMapping("/generate")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String generateForm(Model model) {
		model.addAttribute("activeYear", years.requireActive());
		model.addAttribute("grades", service.allGrades());
		if (!model.containsAttribute("billingMonth"))
			model.addAttribute("billingMonth", LocalDate.now().withDayOfMonth(1));
		return "billing/invoice-generation";
	}

	@PostMapping("/generate/all")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String generateAll(@RequestParam LocalDate billingMonth, Authentication auth, RedirectAttributes redirect) {
		try {
			int n = service.generateForAllGrades(billingMonth, auth.getName());
			redirect.addFlashAttribute("message", n + " invoice(s) generated for the active academic year.");
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/generate";
	}

	@PostMapping("/generate/grade")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String generateGrade(@RequestParam Long gradeId, @RequestParam LocalDate billingMonth, Authentication auth,
			RedirectAttributes redirect) {
		try {
			int n = service.generateForGrade(gradeId, billingMonth, auth.getName());
			redirect.addFlashAttribute("message", n + " invoice(s) generated for the selected grade.");
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/generate";
	}

	@GetMapping("/approval/discount-requests")
	@PreAuthorize("hasRole('ADMIN')")
	public String discountRequests(Model model) {
		model.addAttribute("requests", service.activeDiscounts());
		model.addAttribute("historical", false);
		return "approval/discount-requests";
	}

	@GetMapping("/approval/discount-requests/history")
	@PreAuthorize("hasRole('ADMIN')")
	public String discountHistory(Model model) {
		model.addAttribute("requests", service.discountHistory());
		model.addAttribute("historical", true);
		return "approval/discount-requests";
	}

	@PostMapping("/approval/discount-requests/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public String approveDiscount(@PathVariable Long id, Authentication auth, RedirectAttributes r) {
		try {
			service.approveDiscount(id, auth.getName());
			r.addFlashAttribute("message", "Discount request approved and invoice net amount adjusted.");
		} catch (RuntimeException ex) {
			r.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/approval/discount-requests";
	}

	@PostMapping("/approval/discount-requests/{id}/reject")
	@PreAuthorize("hasRole('ADMIN')")
	public String rejectDiscount(@PathVariable Long id, Authentication auth, RedirectAttributes r) {
		try {
			service.rejectDiscount(id, auth.getName());
			r.addFlashAttribute("message", "Discount request rejected.");
		} catch (RuntimeException ex) {
			r.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/approval/discount-requests";
	}

	@GetMapping("/approval/cancellation-requests")
	@PreAuthorize("hasRole('ADMIN')")
	public String cancellationRequests(Model model) {
		model.addAttribute("requests", service.activeCancellations());
		model.addAttribute("historical", false);
		return "approval/cancellation-requests";
	}

	@GetMapping("/approval/cancellation-requests/history")
	@PreAuthorize("hasRole('ADMIN')")
	public String cancellationHistory(Model model) {
		model.addAttribute("requests", service.cancellationHistory());
		model.addAttribute("historical", true);
		return "approval/cancellation-requests";
	}

	@PostMapping("/approval/cancellation-requests/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public String approveCancellation(@PathVariable Long id, Authentication auth, RedirectAttributes r) {
		try {
			service.approveCancellation(id, auth.getName());
			r.addFlashAttribute("message", "Cancellation request approved; invoice is now CANCELLED.");
		} catch (RuntimeException ex) {
			r.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/approval/cancellation-requests";
	}

	@PostMapping("/approval/cancellation-requests/{id}/reject")
	@PreAuthorize("hasRole('ADMIN')")
	public String rejectCancellation(@PathVariable Long id, Authentication auth, RedirectAttributes r) {
		try {
			service.rejectCancellation(id, auth.getName());
			r.addFlashAttribute("message", "Cancellation request rejected.");
		} catch (RuntimeException ex) {
			r.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/approval/cancellation-requests";
	}
}
