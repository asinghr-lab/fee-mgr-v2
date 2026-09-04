package com.discover.app.school.repository;
import com.discover.app.school.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment,Long> {
 List<StudentEnrollment> findByStatusOrderByRequestDateAsc(EnrollmentStatus status);
 boolean existsByStudentIdAndAcademicYearIdAndStatus(Long studentId,Long academicYearId,EnrollmentStatus status);
 @Query("select e from StudentEnrollment e join fetch e.student join fetch e.academicYear join fetch e.grade where e.status=:status order by e.requestDate asc")
 List<StudentEnrollment> findRequestsWithDetails(@Param("status") EnrollmentStatus status);
}
