package com.discover.app.billing.web;

import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.service.FeeConfigurationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequestMapping("/billing")
public class BillingWebController {
    private final FeeConfigurationService service;
    public BillingWebController(FeeConfigurationService service){this.service=service;}

    @GetMapping("/fee-components")
    public String components(Model model){model.addAttribute("components",service.allComponents()); return "billing/fee-components";}

    @GetMapping("/fee-components/new")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String componentForm(Model model){model.addAttribute("request",new FeeComponentRequest(""));return "billing/fee-component-form";}

    @PostMapping("/fee-components")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String createComponent(@Valid @ModelAttribute("request") FeeComponentRequest request,BindingResult result,RedirectAttributes redirect){
        if(result.hasErrors()) return "billing/fee-component-form";
        try{service.createComponent(request.name());redirect.addFlashAttribute("message","Fee component created successfully.");}
        catch(IllegalArgumentException ex){redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/billing/fee-components";
    }
    @PostMapping("/fee-components/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateComponent(@PathVariable Long id,RedirectAttributes redirect){try{service.deactivateComponent(id);redirect.addFlashAttribute("message","Fee component marked INACTIVE.");}catch(IllegalArgumentException|IllegalStateException ex){redirect.addFlashAttribute("error",ex.getMessage());}return "redirect:/billing/fee-components";}

    @GetMapping("/fee-structures")
    public String structures(Model model){model.addAttribute("structures",service.allStructures());return "billing/fee-structures";}

    @GetMapping("/fee-structures/new")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String structureForm(Model model){
        model.addAttribute("components",service.activeComponents());
        if(!model.containsAttribute("request")) model.addAttribute("request",new FeeStructureRequest("",new ArrayList<>(List.of(new FeeStructureItemRequest(null,null,null)))));
        return "billing/fee-structure-form";
    }
    @PostMapping("/fee-structures")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String createStructure(@Valid @ModelAttribute("request") FeeStructureRequest request,BindingResult result,Model model,RedirectAttributes redirect){
        if(result.hasErrors()){model.addAttribute("components",service.activeComponents());return "billing/fee-structure-form";}
        try{service.createStructure(request.name(),request.items());redirect.addFlashAttribute("message","Fee structure created successfully and is immutable.");return "redirect:/billing/fee-structures";}
        catch(IllegalArgumentException ex){model.addAttribute("components",service.activeComponents());model.addAttribute("error",ex.getMessage());return "billing/fee-structure-form";}
    }
    @PostMapping("/fee-structures/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateStructure(@PathVariable Long id,RedirectAttributes redirect){try{service.deactivateStructure(id);redirect.addFlashAttribute("message","Fee structure marked INACTIVE. Existing grade assignments remain intact.");}catch(IllegalArgumentException|IllegalStateException ex){redirect.addFlashAttribute("error",ex.getMessage());}return "redirect:/billing/fee-structures";}

    @GetMapping("/grade-fee-structures")
    public String assignments(Model model){model.addAttribute("assignments",service.assignments());return "billing/grade-fee-structures";}
    @GetMapping("/grade-fee-structures/new")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String assignmentForm(Model model){model.addAttribute("grades",service.allGrades());model.addAttribute("structures",service.activeStructures());model.addAttribute("request",new GradeFeeStructureRequest(null,null,java.time.LocalDate.now()));return "billing/grade-fee-structure-form";}
    @PostMapping("/grade-fee-structures")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String assign(@Valid @ModelAttribute("request") GradeFeeStructureRequest request,BindingResult result,Model model,RedirectAttributes redirect){
        if(result.hasErrors()){model.addAttribute("grades",service.allGrades());model.addAttribute("structures",service.activeStructures());return "billing/grade-fee-structure-form";}
        try{service.assignToGrade(request.gradeId(),request.feeStructureId(),request.effectiveFrom());redirect.addFlashAttribute("message","Fee structure assigned to grade successfully.");return "redirect:/billing/grade-fee-structures";}
        catch(IllegalArgumentException ex){model.addAttribute("grades",service.allGrades());model.addAttribute("structures",service.activeStructures());model.addAttribute("error",ex.getMessage());return "billing/grade-fee-structure-form";}
    }
    @GetMapping("/grade-fee-structures/{gradeId}/history")
    public String assignmentHistory(@PathVariable Long gradeId,Model model){model.addAttribute("assignments",service.assignmentHistory(gradeId));model.addAttribute("gradeId",gradeId);return "billing/grade-fee-structure-history";}
}
