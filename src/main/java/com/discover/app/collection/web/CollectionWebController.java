package com.discover.app.collection.web;

import com.discover.app.collection.service.CollectionService;
import com.discover.app.billing.service.BillingService;
import com.discover.app.school.service.AcademicYearService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.*;
import java.time.*;

@Controller
@RequestMapping("/collection")
public class CollectionWebController {
	private final CollectionService collection;
	private final BillingService billing;
	private final AcademicYearService years;

	public CollectionWebController(CollectionService c, BillingService b, AcademicYearService y) {
		collection = c;
		billing = b;
		years = y;
	}

	@GetMapping("/payments")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String payments(@RequestParam(defaultValue = "") String term, @RequestParam(defaultValue = "0") int page,
			Model m) {
		m.addAttribute("payments", collection.search(term, page));
		m.addAttribute("term", term);
		m.addAttribute("activeYear", years.requireActive());
		return "collection/payments";
	}

	@GetMapping("/invoices/{id}/pay")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String form(@PathVariable Long id, Model m) {
		var i = billing.detail(id);
		m.addAttribute("invoice", i);
		m.addAttribute("payment", new com.discover.app.collection.dto.CollectionDtos.PaymentRequest(id,
				i.getNetAmount(), LocalDateTime.now(), ""));
		return "collection/payment-form";
	}

	@PostMapping("/payments")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public String save(@RequestParam Long invoiceId, @RequestParam BigDecimal amount, @RequestParam String paidAt,
			@RequestParam(defaultValue = "") String notes, Authentication a, RedirectAttributes r) {
		try {
			collection.record(invoiceId, amount, LocalDateTime.parse(paidAt), notes, a.getName());
			r.addFlashAttribute("message", "Payment recorded successfully.");
		} catch (RuntimeException ex) {
			r.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/billing/invoices/" + invoiceId;
	}
}
