package com.discover.app.school.service;
import com.discover.app.school.domain.School;
import com.discover.app.school.repository.SchoolRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SchoolService {
 private final SchoolRepository repository;
 public SchoolService(SchoolRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public School getSchool(){return repository.findAll().stream().findFirst().orElse(null);}
 @PreAuthorize("hasRole('ADMIN')") @Transactional public School saveOrUpdate(String name,String address,String phone,String email){
   School s=getSchool(); if(s==null){s=new School(name,address,phone,email);} else {s.update(name,address,phone,email);} return repository.save(s);
 }
}
