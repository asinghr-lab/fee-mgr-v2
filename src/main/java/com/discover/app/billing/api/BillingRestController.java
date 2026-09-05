package com.discover.app.billing.api;

import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.service.FeeConfigurationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/billing")
public class BillingRestController {
    private final FeeConfigurationService service;
    public BillingRestController(FeeConfigurationService service){this.service=service;}
    @GetMapping("/fee-components") public Object components(){return service.allComponents().stream().map(service::componentResponse).toList();}
    @GetMapping("/fee-components/active") public Object activeComponents(){return service.activeComponents().stream().map(service::componentResponse).toList();}
    @PostMapping("/fee-components") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Object createComponent(@Valid @RequestBody FeeComponentRequest r){return service.componentResponse(service.createComponent(r.name()));}
    @PostMapping("/fee-components/{id}/deactivate") @PreAuthorize("hasRole('ADMIN')") public void deactivateComponent(@PathVariable Long id){service.deactivateComponent(id);}
    @GetMapping("/fee-structures") public Object structures(){return service.allStructures().stream().map(service::structureResponse).toList();}
    @GetMapping("/fee-structures/active") public Object activeStructures(){return service.activeStructures().stream().map(service::structureResponse).toList();}
    @PostMapping("/fee-structures") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Object createStructure(@Valid @RequestBody FeeStructureRequest r){return service.structureResponse(service.createStructure(r.name(),r.items()));}
    @PostMapping("/fee-structures/{id}/deactivate") @PreAuthorize("hasRole('ADMIN')") public void deactivateStructure(@PathVariable Long id){service.deactivateStructure(id);}
    @GetMapping("/grade-fee-structures") public Object assignments(){return service.assignments().stream().map(service::assignmentResponse).toList();}
    @PostMapping("/grade-fee-structures") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Object assign(@Valid @RequestBody GradeFeeStructureRequest r){return service.assignmentResponse(service.assignToGrade(r.gradeId(),r.feeStructureId(),r.effectiveFrom()));}
    @GetMapping("/grades/{gradeId}/fee-structures/history") public Object history(@PathVariable Long gradeId){return service.assignmentHistory(gradeId).stream().map(service::assignmentResponse).toList();}
}
