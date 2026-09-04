package com.discover.app.school.service;
import com.discover.app.identity.domain.User; import com.discover.app.identity.repository.UserRepository; import com.discover.app.school.domain.*; import com.discover.app.school.repository.*;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime; import java.util.List;
@Service public class EnrollmentService {
 private final StudentEnrollmentRepository enrollments; private final StudentRepository students; private final AcademicYearRepository years; private final GradeRepository grades; private final UserRepository users;
 public EnrollmentService(StudentEnrollmentRepository e,StudentRepository s,AcademicYearRepository y,GradeRepository g,UserRepository u){enrollments=e;students=s;years=y;grades=g;users=u;}
 @PreAuthorize("hasAnyRole('ADMIN','STAFF')") @Transactional public StudentEnrollment request(Long studentId,Long academicYearId,Long gradeId,String username){
  if(enrollments.existsByStudentIdAndAcademicYearIdAndStatus(studentId,academicYearId,EnrollmentStatus.REQUESTED)||enrollments.existsByStudentIdAndAcademicYearIdAndStatus(studentId,academicYearId,EnrollmentStatus.APPROVED)) throw new IllegalStateException("Student already has an active enrollment/request for this academic year");
  Student s=students.findById(studentId).orElseThrow(); AcademicYear y=years.findById(academicYearId).orElseThrow(); Grade g=grades.findById(gradeId).orElseThrow(); User u=users.findByUsername(username).orElseThrow();
  return enrollments.save(new StudentEnrollment(s,y,g,u,LocalDateTime.now()));
 }
 @PreAuthorize("hasRole('ADMIN')") @Transactional public StudentEnrollment approve(Long enrollmentId,String username){
  StudentEnrollment e=enrollments.findById(enrollmentId).orElseThrow(); User admin=users.findByUsername(username).orElseThrow(); e.approve(admin,LocalDateTime.now()); return e;
 }
 @PreAuthorize("hasAnyRole('ADMIN','STAFF')") @Transactional public StudentEnrollment cancel(Long enrollmentId){StudentEnrollment e=enrollments.findById(enrollmentId).orElseThrow();e.cancel();return e;}
 @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly=true) public List<StudentEnrollment> activeRequests(){return enrollments.findRequestsWithDetails(EnrollmentStatus.REQUESTED);}
 @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly=true) public List<StudentEnrollment> historicalRequests(){return java.util.stream.Stream.concat(enrollments.findRequestsWithDetails(EnrollmentStatus.APPROVED).stream(),enrollments.findRequestsWithDetails(EnrollmentStatus.CANCELLED).stream()).sorted(java.util.Comparator.comparing(StudentEnrollment::getRequestDate).reversed()).toList();}
}
