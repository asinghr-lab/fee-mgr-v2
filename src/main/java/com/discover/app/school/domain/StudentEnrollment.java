package com.discover.app.school.domain;

import com.discover.app.identity.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="student_enrollments", indexes={
 @Index(name="idx_enrollment_student_year",columnList="student_id,academic_year_id"),
 @Index(name="idx_enrollment_status",columnList="status")})
public class StudentEnrollment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id",nullable=false) private Student student;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="academic_year_id",nullable=false) private AcademicYear academicYear;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="grade_id",nullable=false) private Grade grade;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private EnrollmentStatus status;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="requested_by_user_id",nullable=false) private User requestedBy;
 @Column(nullable=false) private LocalDateTime requestDate;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by_user_id") private User approvedBy;
 private LocalDateTime approvalDate;
 protected StudentEnrollment() {}
 public StudentEnrollment(Student student,AcademicYear academicYear,Grade grade,User requestedBy,LocalDateTime requestDate){this.student=student;this.academicYear=academicYear;this.grade=grade;this.requestedBy=requestedBy;this.requestDate=requestDate;this.status=EnrollmentStatus.REQUESTED;}
 public Long getId(){return id;} public Student getStudent(){return student;} public AcademicYear getAcademicYear(){return academicYear;} public Grade getGrade(){return grade;} public EnrollmentStatus getStatus(){return status;} public User getRequestedBy(){return requestedBy;} public LocalDateTime getRequestDate(){return requestDate;} public User getApprovedBy(){return approvedBy;} public LocalDateTime getApprovalDate(){return approvalDate;}
 public void approve(User admin,LocalDateTime when){if(status!=EnrollmentStatus.REQUESTED)throw new IllegalStateException("Only requested enrollments can be approved");status=EnrollmentStatus.APPROVED;approvedBy=admin;approvalDate=when;}
 public void cancel(){if(status!=EnrollmentStatus.REQUESTED)throw new IllegalStateException("Only requested enrollments can be cancelled");status=EnrollmentStatus.CANCELLED;}
}
