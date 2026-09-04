package com.discover.app.school.service;
import com.discover.app.school.domain.AcademicYear;
import com.discover.app.school.repository.AcademicYearRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate; import java.util.List;
@Service
public class AcademicYearService {
 private final AcademicYearRepository repository;
 public AcademicYearService(AcademicYearRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<AcademicYear> findAll(){return repository.findAllByOrderByStartDateDesc();}
 @PreAuthorize("hasRole('ADMIN')") @Transactional public AcademicYear create(String name,LocalDate start,LocalDate end){
  if(!end.isAfter(start)) throw new IllegalArgumentException("End date must be after start date");
  return repository.save(new AcademicYear(name,start,end));
 }
 @PreAuthorize("hasRole('ADMIN')") @Transactional public void deactivate(Long id){repository.findById(id).orElseThrow().deactivate();}
}
