package com.discover.app.billing.repository;
import com.discover.app.billing.domain.*;
import com.discover.app.school.domain.Grade;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.*;
public interface GradeFeeStructureRepository extends JpaRepository<GradeFeeStructure,Long> {
    @Query("select g from GradeFeeStructure g join fetch g.grade gr join fetch g.feeStructure fs order by gr.displayOrder asc, g.effectiveFrom desc, g.id desc")
    List<GradeFeeStructure> findAllForDisplay();
    @Query("select g from GradeFeeStructure g where g.grade = :grade and g.effectiveFrom <= :onDate and (g.effectiveTo is null or g.effectiveTo >= :onDate) order by g.effectiveFrom desc, g.id desc")
    List<GradeFeeStructure> findActiveForGrade(Grade grade, LocalDate onDate);
    @Query("select g from GradeFeeStructure g where g.grade.id = :gradeId and g.effectiveFrom <= :onDate and (g.effectiveTo is null or g.effectiveTo >= :onDate) order by g.effectiveFrom desc, g.id desc")
    List<GradeFeeStructure> findActiveForGradeId(Long gradeId, LocalDate onDate);
    List<GradeFeeStructure> findByGradeOrderByEffectiveFromDescIdDesc(Grade grade);
}
