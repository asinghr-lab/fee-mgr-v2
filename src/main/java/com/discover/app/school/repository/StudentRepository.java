package com.discover.app.school.repository;
import com.discover.app.school.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student,Long> {
 Optional<Student> findByAdmissionNumber(String admissionNumber);
 List<Student> findAllByOrderByAdmissionNumberAsc();
 boolean existsByAdmissionNumber(String admissionNumber);
 boolean existsByPhoneNumber(String phoneNumber);
}
