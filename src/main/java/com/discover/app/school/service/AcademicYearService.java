package com.discover.app.school.service;
import com.discover.app.school.domain.AcademicYear;
import com.discover.app.school.repository.AcademicYearRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate; import java.util.List;
@Service public class AcademicYearService {
 private final AcademicYearRepository repository;
 public AcademicYearService(AcademicYearRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<AcademicYear> findAll(){return repository.findAllByOrderByStartDateDesc();}
 @Transactional(readOnly=true) public AcademicYear getActive(){return repository.findFirstByActiveTrueOrderByStartDateDesc().orElse(null);}
 @Transactional(readOnly=true) public AcademicYear requireActive(){return repository.findFirstByActiveTrueOrderByStartDateDesc().orElseThrow(()->new IllegalStateException("No active academic year is configured."));}
 @PreAuthorize("hasRole('ADMIN')") @Transactional public AcademicYear create(String name,LocalDate start,LocalDate end){
  String n=name==null?"":name.trim(); if(n.isBlank()) throw new IllegalArgumentException("Academic year name is required.");
  if(!end.isAfter(start)) throw new IllegalArgumentException("End date must be after start date");
  repository.findByNameIgnoreCase(n).ifPresent(x->{throw new IllegalArgumentException("An academic year with this name already exists.");});
  repository.findByActiveTrueOrderByStartDateDesc().forEach(AcademicYear::deactivate);
  AcademicYear year=new AcademicYear(n,start,end); year.activate(); return repository.save(year);
 }
 @PreAuthorize("hasRole('ADMIN')") @Transactional public void activate(Long id){
  AcademicYear target=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Academic year not found."));
  repository.findByActiveTrueOrderByStartDateDesc().forEach(y->{if(!y.getId().equals(id)) y.deactivate();}); target.activate(); repository.save(target);
 }
 @PreAuthorize("hasRole('ADMIN')") @Transactional public void deactivate(Long id){
  AcademicYear target=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Academic year not found."));
  if(target.isActive()) throw new IllegalStateException("The active academic year cannot be deactivated. Activate another academic year first.");
  target.deactivate();
 }
}
