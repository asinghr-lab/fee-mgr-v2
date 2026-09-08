package com.discover.app.billing.api;

import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.service.BillingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingInvoiceRestController {
	private final BillingService service;

	public BillingInvoiceRestController(BillingService service) {
		this.service = service;
	}

	@GetMapping("/invoices/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public InvoiceResponse invoice(@PathVariable Long id) {
		return service.response(service.detail(id));
	}
}
