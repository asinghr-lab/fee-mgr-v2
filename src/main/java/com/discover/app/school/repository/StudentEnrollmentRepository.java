package com.discover.app.school.repository;
import com.discover.app.school.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment,Long> {
 List<StudentEnrollment> findByStatusOrderByRequestDateAsc(EnrollmentStatus status);
 boolean existsByStudentIdAndAcademicYearIdAndStatus(Long studentId,Long academicYearId,EnrollmentStatus status);
 @Query("select e from StudentEnrollment e join fetch e.student join fetch e.academicYear join fetch e.grade where e.status=:status order by e.requestDate asc") List<StudentEnrollment> findRequestsWithDetails(@Param("status") EnrollmentStatus status);
 @Query("select e from StudentEnrollment e join fetch e.student s join fetch e.grade g join fetch e.academicYear y where e.academicYear.id=:yearId and e.status=:status order by g.displayOrder asc, g.id asc, s.admissionNumber asc") List<StudentEnrollment> findApprovedByAcademicYear(@Param("yearId") Long yearId,@Param("status") EnrollmentStatus status);
 @Query("select e from StudentEnrollment e join fetch e.student s join fetch e.grade g join fetch e.academicYear y where e.academicYear.id=:yearId and e.grade.id=:gradeId and e.status=:status order by s.admissionNumber asc") List<StudentEnrollment> findApprovedByAcademicYearAndGrade(@Param("yearId") Long yearId,@Param("gradeId") Long gradeId,@Param("status") EnrollmentStatus status);
}
