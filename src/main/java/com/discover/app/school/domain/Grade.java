package com.discover.app.school.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(name = "uk_grade_school_name", columnNames = {
		"school_id", "name" }))
public class Grade {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;
	@Column(nullable = false, length = 100)
	private String name;
	@Column(nullable = false, length = 100)
	private String section;
	@Column(nullable = true, length = 100)
	private String description;
	@Column(nullable = false)
	private Integer displayOrder;

	protected Grade() {
	}

	public Grade(School school, String name, String section,  String description,  Integer displayOrder) {
		this.school = school;
		this.name = name;
		this.section = section;
		this.description = description;
		this.displayOrder = displayOrder;
	}

	public Long getId() {
		return id;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public School getSchool() {
		return school;
	}

	public String getName() {
		return name;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void update(String name, Integer displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}
}
