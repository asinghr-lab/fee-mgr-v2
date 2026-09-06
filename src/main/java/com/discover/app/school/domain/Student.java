package com.discover.app.school.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="students", uniqueConstraints=@UniqueConstraint(name="uk_student_admission_number",columnNames="admission_number"))
public class Student {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="admission_number",nullable=false,length=50) private String admissionNumber;
 @Column(nullable=false,length=120) private String firstName;
 @Column(length=120) private String lastName;
 @Column(nullable=false) private LocalDate admissionDate;
 @Column private LocalDate dateOfBirth;
 @Column(length=20) private String gender;
 @Column(length=20,unique=true) private String phoneNumber;
 @Column(length=150) private String email;
 protected Student() {}
 public Student(String admissionNumber,String firstName,String lastName,LocalDate admissionDate,LocalDate dateOfBirth,String gender,String phoneNumber,String email){this.admissionNumber=admissionNumber;this.firstName=firstName;this.lastName=lastName;this.admissionDate=admissionDate;this.dateOfBirth=dateOfBirth;this.gender=gender;this.phoneNumber=phoneNumber;this.email=email;}
 public Long getId(){return id;} public String getAdmissionNumber(){return admissionNumber;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public LocalDate getAdmissionDate(){return admissionDate;} public LocalDate getDateOfBirth(){return dateOfBirth;} public String getGender(){return gender;} public String getPhoneNumber(){return phoneNumber;} public String getEmail(){return email;}
 public String getFullName(){return lastName==null||lastName.isBlank()?firstName:firstName+" "+lastName;}
}
