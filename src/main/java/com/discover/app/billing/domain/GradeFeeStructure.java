package com.discover.app.billing.domain;

import com.discover.app.school.domain.Grade;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="grade_fee_structures", indexes={@Index(name="idx_gfs_grade_dates",columnList="grade_id,effective_from,effective_to"),@Index(name="idx_gfs_structure",columnList="fee_structure_id")})
public class GradeFeeStructure {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="grade_id",nullable=false) private Grade grade;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="fee_structure_id",nullable=false) private FeeStructure feeStructure;
    @Column(name="effective_from",nullable=false) private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
    protected GradeFeeStructure() {}
    public GradeFeeStructure(Grade grade,FeeStructure feeStructure,LocalDate effectiveFrom){this.grade=grade;this.feeStructure=feeStructure;this.effectiveFrom=effectiveFrom;}
    @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
    public void closeOn(LocalDate date){if(date.isBefore(effectiveFrom)) throw new IllegalArgumentException("Effective-to cannot be before effective-from"); effectiveTo=date;}
    public boolean isActiveOn(LocalDate date){return !date.isBefore(effectiveFrom)&&(effectiveTo==null||!date.isAfter(effectiveTo));}
    public Long getId(){return id;} public Grade getGrade(){return grade;} public FeeStructure getFeeStructure(){return feeStructure;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public LocalDateTime getCreatedAt(){return createdAt;}
}
