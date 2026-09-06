package com.discover.app.school.service;

import com.discover.app.school.domain.Student;
import com.discover.app.school.repository.StudentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service public class StudentService {
 private final StudentRepository repository; public StudentService(StudentRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<Student> findAll(){return repository.findAllByOrderByAdmissionNumberAsc();}
 public Student findById(Long id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Student not found"));}
 @PreAuthorize("hasAnyRole('ADMIN','STAFF')") @Transactional public Student create(String admissionNumber,String firstName,String lastName,LocalDate dob,String gender,String phone,String email){
  if(repository.existsByAdmissionNumber(admissionNumber)) throw new IllegalArgumentException("Admission number already exists");
  if(phone!=null&&!phone.isBlank()&&repository.existsByPhoneNumber(phone)) throw new IllegalArgumentException("Phone number already exists");
  if(dob!=null&&dob.isAfter(LocalDate.now())) throw new IllegalArgumentException("Date of birth cannot be in the future");
  return repository.save(new Student(admissionNumber,firstName,lastName,LocalDate.now(),dob,gender,phone,email));
 }
}
