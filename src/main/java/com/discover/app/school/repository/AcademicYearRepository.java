package com.discover.app.school.repository;
import com.discover.app.school.domain.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AcademicYearRepository extends JpaRepository<AcademicYear,Long> {
 List<AcademicYear> findAllByOrderByStartDateDesc();
 Optional<AcademicYear> findFirstByActiveTrueOrderByStartDateDesc();
 List<AcademicYear> findByActiveTrueOrderByStartDateDesc();
 Optional<AcademicYear> findByNameIgnoreCase(String name);
}
