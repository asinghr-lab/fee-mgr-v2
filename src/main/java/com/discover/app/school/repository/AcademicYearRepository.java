package com.discover.app.school.repository;
import com.discover.app.school.domain.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AcademicYearRepository extends JpaRepository<AcademicYear,Long> {
 List<AcademicYear> findAllByOrderByStartDateDesc();
 List<AcademicYear> findByActiveTrueOrderByStartDateDesc();
}
