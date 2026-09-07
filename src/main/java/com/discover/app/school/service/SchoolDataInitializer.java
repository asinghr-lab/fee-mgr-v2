package com.discover.app.school.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.discover.app.identity.domain.Role;
import com.discover.app.identity.domain.User;
import com.discover.app.identity.domain.UserProfile;
import com.discover.app.identity.repository.UserProfileRepository;
import com.discover.app.identity.repository.UserRepository;
import com.discover.app.school.domain.AcademicYear;
import com.discover.app.school.domain.EnrollmentStatus;
import com.discover.app.school.domain.Grade;
import com.discover.app.school.domain.School;
import com.discover.app.school.domain.Student;
import com.discover.app.school.domain.StudentEnrollment;
import com.discover.app.school.repository.AcademicYearRepository;
import com.discover.app.school.repository.GradeRepository;
import com.discover.app.school.repository.SchoolRepository;
import com.discover.app.school.repository.StudentEnrollmentRepository;
import com.discover.app.school.repository.StudentRepository;

@Configuration
public class SchoolDataInitializer {

	@Bean
	CommandLineRunner initializeSchool(SchoolRepository schoolRepo, GradeRepository gradeRepo,
			AcademicYearRepository yearRepo, StudentRepository studentRepo, StudentEnrollmentRepository enrollmentRepo,
			UserRepository userRepo) {
		return args -> {

			if (schoolRepo.findById((long) 1).isEmpty()) {
				var school = new School("Discover International School", "Address of School, 246149, Kotdwara",
						"9898221133", "abc@xyz.com");
				schoolRepo.save(school);

				if (yearRepo.findById((long) 1).isEmpty()) {
					var year = new AcademicYear("2026-2027", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));
					year.activate();
					yearRepo.save(year);

					if (gradeRepo.findById((long) 1).isEmpty()) {
						/*
						 * var gr1 = new Grade(school, "Grade1", "A", "Grade1- Section A", 1); var gr2 =
						 * new Grade(school, "Grade2", "A", "Grade2- Section A", 2); var gr3 = new
						 * Grade(school, "Grade3", "A", "Grade3- Section A", 3); var gr4 = new
						 * Grade(school, "Grade4", "A", "Grade4- Section A", 4); var gr5 = new
						 * Grade(school, "Grade5", "A", "Grade5- Section A", 5);
						 */
						var grade1 = new Grade(school, "Grade1", 1);
						var grade2 = new Grade(school, "Grade2", 2);
						var grade3 = new Grade(school, "Grade3", 3);
						var grade4 = new Grade(school, "Grade4", 4);
						var grade5 = new Grade(school, "Grade5", 5);

						gradeRepo.save(grade1);
						gradeRepo.save(grade2);
						gradeRepo.save(grade3);
						gradeRepo.save(grade4);
						gradeRepo.save(grade5);

						var student1 = new Student("A001", "Test", "Student1", LocalDate.now(), null, null, null, null);
						var student2 = new Student("A002", "Test", "Student2", LocalDate.now(), null, null, null, null);
						var student3 = new Student("A003", "Test", "Student3", LocalDate.now(), null, null, null, null);
						var student4 = new Student("A004", "Test", "Student4", LocalDate.now(), null, null, null, null);
						var student5 = new Student("A005", "Test", "Student5", LocalDate.now(), null, null, null, null);
						var student6 = new Student("A006", "Test", "Student6", LocalDate.now(), null, null, null, null);

						studentRepo.save(student1);
						studentRepo.save(student2);
						studentRepo.save(student3);
						studentRepo.save(student4);
						studentRepo.save(student5);
						studentRepo.save(student6);

						var user = userRepo.findByUsername("staff").stream().findFirst().get();
						// var user=new com.discover.app.identity.domain.User("staff","x",true);

						var enrollment1 = new StudentEnrollment(student1, year, grade1, user, LocalDateTime.now());
						var enrollment2 = new StudentEnrollment(student2, year, grade1, user, LocalDateTime.now());
						var enrollment3 = new StudentEnrollment(student3, year, grade2, user, LocalDateTime.now());
						var enrollment4 = new StudentEnrollment(student4, year, grade2, user, LocalDateTime.now());
						var enrollment5 = new StudentEnrollment(student5, year, grade3, user, LocalDateTime.now());
						var enrollment6 = new StudentEnrollment(student6, year, grade3, user, LocalDateTime.now());

						enrollmentRepo.save(enrollment1);
						enrollmentRepo.save(enrollment2);
						enrollmentRepo.save(enrollment3);
						enrollmentRepo.save(enrollment4);
						enrollmentRepo.save(enrollment5);
						enrollmentRepo.save(enrollment6);

					}
				}
			}
		};
	}
}
