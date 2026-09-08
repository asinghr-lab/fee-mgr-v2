package com.discover.app.billing.domain;

import com.discover.app.school.domain.Grade;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade_fee_structures", indexes = {
        @Index(name = "idx_gfs_grade", columnList = "grade_id"),
        @Index(name = "idx_gfs_structure", columnList = "fee_structure_id") })
public class GradeFeeStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected GradeFeeStructure() {}

    public GradeFeeStructure(Grade grade, FeeStructure feeStructure) {
        this.grade = grade;
        this.feeStructure = feeStructure;
    }

    public void changeFeeStructure(FeeStructure feeStructure) { this.feeStructure = feeStructure; }

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Grade getGrade() { return grade; }
    public FeeStructure getFeeStructure() { return feeStructure; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
