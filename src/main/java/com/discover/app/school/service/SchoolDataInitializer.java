package com.discover.app.school.service;

import java.time.LocalDate;

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
import com.discover.app.school.domain.Grade;
import com.discover.app.school.domain.School;
import com.discover.app.school.repository.AcademicYearRepository;
import com.discover.app.school.repository.GradeRepository;
import com.discover.app.school.repository.SchoolRepository;

@Configuration
public class SchoolDataInitializer {

	@Bean
	CommandLineRunner initializeSchool(SchoolRepository school, GradeRepository grade, AcademicYearRepository year) {
		return args -> {

			School myschool = null;
			if (school.findById((long) 1).isEmpty()) {
				var s = new School("Discover International School", "Address of School, 246149, Kotdwara", "9898221133",
						"abc@xyz.com");
				school.save(s);
				myschool = s;

				if (year.findById((long) 1).isEmpty()) {
					var yr = new AcademicYear("2026-2027", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));
					year.save(yr);
				}

				if (grade.findById((long) 1).isEmpty()) {
					/*
					 * var gr1 = new Grade(myschool, "Grade1", "A", "Grade1- Section A", 1);
					var gr2 = new Grade(myschool, "Grade2", "A", "Grade2- Section A", 2);
					var gr3 = new Grade(myschool, "Grade3", "A", "Grade3- Section A", 3);
					var gr4 = new Grade(myschool, "Grade4", "A", "Grade4- Section A", 4);
					var gr5 = new Grade(myschool, "Grade5", "A", "Grade5- Section A", 5);
					*/
					var gr1 = new Grade(myschool, "Grade1",  1);
					var gr2 = new Grade(myschool, "Grade2",  2);
					var gr3 = new Grade(myschool, "Grade3",  3);
					var gr4 = new Grade(myschool, "Grade4",  4);
					var gr5 = new Grade(myschool, "Grade5",  5);
					
					grade.save(gr1);
					grade.save(gr2);
					grade.save(gr3);
					grade.save(gr4);
					grade.save(gr5);
				}
			}
		};
	}
}
