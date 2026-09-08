package com.discover.app.billing.repository;

import com.discover.app.billing.domain.*;
import com.discover.app.school.domain.Grade;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface GradeFeeStructureRepository extends JpaRepository<GradeFeeStructure, Long> {
    @Query("select g from GradeFeeStructure g join fetch g.grade gr join fetch g.feeStructure fs order by gr.displayOrder asc, gr.name asc, g.id desc")
    List<GradeFeeStructure> findAllForDisplay();

    @Query("select g from GradeFeeStructure g join fetch g.feeStructure fs where g.grade.id=:gradeId order by g.id desc")
    List<GradeFeeStructure> findForGradeId(@Param("gradeId") Long gradeId);

    List<GradeFeeStructure> findByGradeOrderByIdDesc(Grade grade);

    @Query("select g from GradeFeeStructure g join fetch g.grade gr join fetch g.feeStructure fs where g.id in (select max(g2.id) from GradeFeeStructure g2 group by g2.grade.id) order by gr.displayOrder asc, gr.name asc")
    List<GradeFeeStructure> findCurrentAssignments();
}
