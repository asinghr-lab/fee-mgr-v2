package com.discover.app.billing.service;

import com.discover.app.billing.domain.*;
import com.discover.app.billing.dto.BillingDtos.*;
import com.discover.app.billing.repository.*;
import com.discover.app.school.domain.Grade;
import com.discover.app.school.repository.GradeRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeeConfigurationService {
    private final FeeComponentRepository components;
    private final FeeStructureRepository structures;
    private final GradeFeeStructureRepository assignments;
    private final GradeRepository grades;
    public FeeConfigurationService(FeeComponentRepository components,FeeStructureRepository structures,GradeFeeStructureRepository assignments,GradeRepository grades){this.components=components;this.structures=structures;this.assignments=assignments;this.grades=grades;}

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public FeeComponent createComponent(String name){
        String n=name.trim();
        components.findByNameIgnoreCase(n).ifPresent(x->{throw new IllegalArgumentException("A fee component with this name already exists.");});
        return components.save(new FeeComponent(n));
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateComponent(Long id){components.findById(id).orElseThrow(()->new IllegalArgumentException("Fee component not found.")).deactivate();}
    @PreAuthorize("isAuthenticated()") public List<FeeComponent> allComponents(){return components.findAllByOrderByUpdatedAtDescIdDesc();}
    @PreAuthorize("isAuthenticated()") public List<FeeComponent> activeComponents(){return components.findByStatusOrderByNameAsc(FeeStatus.ACTIVE);}

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public FeeStructure createStructure(String name,List<FeeStructureItemRequest> itemRequests){
        String n=name.trim();
        structures.findByNameIgnoreCase(n).ifPresent(x->{throw new IllegalArgumentException("A fee structure with this name already exists.");});
        if(itemRequests==null||itemRequests.isEmpty()) throw new IllegalArgumentException("At least one fee structure item is required.");
        FeeStructure s=new FeeStructure(n);
        Set<Long> seen=new HashSet<>();
        for(var r:itemRequests){
            if(!seen.add(r.feeComponentId())) throw new IllegalArgumentException("A fee component can occur only once in a structure.");
            FeeComponent c=components.findById(r.feeComponentId()).orElseThrow(()->new IllegalArgumentException("Fee component not found."));
            if(c.getStatus()!=FeeStatus.ACTIVE) throw new IllegalArgumentException("Only active fee components can be added to a new structure.");
            s.addItem(new FeeStructureItem(c,r.frequency(),r.amount()));
        }
        return structures.save(s);
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateStructure(Long id){structures.findById(id).orElseThrow(()->new IllegalArgumentException("Fee structure not found.")).deactivate();}
    @PreAuthorize("isAuthenticated()") public List<FeeStructure> allStructures(){return structures.findAllByOrderByUpdatedAtDescIdDesc();}
    @PreAuthorize("isAuthenticated()") public List<FeeStructure> activeStructures(){return structures.findByStatusOrderByNameAsc(FeeStatus.ACTIVE);}

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public GradeFeeStructure assignToGrade(Long gradeId,Long structureId,LocalDate effectiveFrom){
        Grade grade=grades.findById(gradeId).orElseThrow(()->new IllegalArgumentException("Grade not found."));
        FeeStructure structure=structures.findById(structureId).orElseThrow(()->new IllegalArgumentException("Fee structure not found."));
        if(structure.getStatus()!=FeeStatus.ACTIVE) throw new IllegalArgumentException("Only active fee structures can be assigned to a grade.");
        LocalDate date=effectiveFrom;
        assignments.findActiveForGradeId(gradeId,date).stream().findFirst().ifPresent(current->{
            if(current.getFeeStructure().getId().equals(structureId)) throw new IllegalArgumentException("This fee structure is already active for the grade.");
            if(!date.isAfter(current.getEffectiveFrom())) throw new IllegalArgumentException("The new effective date must be after the current assignment effective date.");
            current.closeOn(date.minusDays(1));
        });
        return assignments.save(new GradeFeeStructure(grade,structure,date));
    }
    @PreAuthorize("isAuthenticated()") public List<GradeFeeStructure> assignments(){return assignments.findAllForDisplay();}
    @PreAuthorize("isAuthenticated()") public List<GradeFeeStructure> assignmentHistory(Long gradeId){return assignments.findByGradeOrderByEffectiveFromDescIdDesc(grades.findById(gradeId).orElseThrow());}
    @PreAuthorize("isAuthenticated()") public List<Grade> allGrades(){return grades.findAllByOrderByDisplayOrderAscNameAsc();}

    public FeeComponentResponse componentResponse(FeeComponent c){return new FeeComponentResponse(c.getId(),c.getName(),c.getStatus());}
    public FeeStructureResponse structureResponse(FeeStructure s){return new FeeStructureResponse(s.getId(),s.getName(),s.getStatus(),s.getItems().stream().map(i->new FeeStructureItemResponse(i.getId(),i.getFeeComponent().getName(),i.getFrequency(),i.getAmount())).collect(Collectors.toList()));}
    public GradeFeeStructureResponse assignmentResponse(GradeFeeStructure a){return new GradeFeeStructureResponse(a.getId(),a.getGrade().getId(),a.getGrade().getName(),a.getFeeStructure().getId(),a.getFeeStructure().getName(),a.getEffectiveFrom(),a.getEffectiveTo());}
}
