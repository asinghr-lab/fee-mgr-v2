package com.discover.app.collection.api;

import com.discover.app.collection.service.CollectionService;
import com.discover.app.collection.dto.CollectionDtos.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.*;

@RestController
@RequestMapping("/api/collection/payments")
public class CollectionRestController {
	private final CollectionService service;

	public CollectionRestController(CollectionService s) {
		service = s;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
	public PaymentResponse create(@RequestBody PaymentRequest r, Authentication a) {
		return service.response(service.record(r.invoiceId(), r.amount(), r.paidAt(), r.notes(), a.getName()));
	}
}
