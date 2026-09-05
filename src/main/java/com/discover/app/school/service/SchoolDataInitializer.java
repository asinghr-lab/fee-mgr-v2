package com.discover.app.school.service;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.discover.app.school.domain.AcademicYear;
import com.discover.app.school.domain.Grade;
import com.discover.app.school.domain.School;
import com.discover.app.school.repository.AcademicYearRepository;
import com.discover.app.school.repository.GradeRepository;
import com.discover.app.school.repository.SchoolRepository;

@Configuration
public class SchoolDataInitializer {

	@Bean
	CommandLineRunner initializeSchool(SchoolRepository schoolRepo, GradeRepository gradeRepo,
			AcademicYearRepository yearRepo) {
		return args -> {

			School myschool = null;
			if (schoolRepo.findById((long) 1).isEmpty()) {
				var school = new School("Discover International School", "Address of School, 246149, Kotdwara",
						"9898221133",
						"abc@xyz.com");
				schoolRepo.save(school);
				myschool = school;

				if (yearRepo.findById((long) 1).isEmpty()) {
					var year = new AcademicYear("2026-2027", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));
					yearRepo.save(year);
				}

				if (gradeRepo.findById((long) 1).isEmpty()) {
					var grade1 = new Grade(myschool, "Grade1", "A", "Grade1- Section A", 1);
					var grade2 = new Grade(myschool, "Grade2", "A", "Grade2- Section A", 2);
					var grade3 = new Grade(myschool, "Grade3", "A", "Grade3- Section A", 3);
					var grade4 = new Grade(myschool, "Grade4", "A", "Grade4- Section A", 4);
					var grade5 = new Grade(myschool, "Grade5", "A", "Grade5- Section A", 5);

					gradeRepo.save(grade1);
					gradeRepo.save(grade2);
					gradeRepo.save(grade3);
					gradeRepo.save(grade4);
					gradeRepo.save(grade5);
				}
			}
		};
	}
}
