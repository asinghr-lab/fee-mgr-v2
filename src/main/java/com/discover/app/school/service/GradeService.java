package com.discover.app.school.service;

import com.discover.app.school.domain.*;
import com.discover.app.school.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GradeService {
	private final GradeRepository grades;
	private final SchoolRepository schools;

	public GradeService(GradeRepository grades, SchoolRepository schools) {
		this.grades = grades;
		this.schools = schools;
	}

	@Transactional(readOnly = true)
	public List<Grade> findAll() {
		return grades.findAllByOrderByDisplayOrderAscNameAsc();
	}

	public Grade findById(Long id) {
		return grades.findById(id).orElseThrow(() -> new IllegalArgumentException("Grade not found"));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public Grade create(String name, String section, String description, Integer order) {
		School s = schools.findAll().stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("School is not configured"));
		return grades.save(new Grade(s, name, section, description, order));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public Grade update(Long id, String name, Integer order) {
		Grade g = grades.findById(id).orElseThrow();
		g.update(name, order);
		return g;
	}
}
