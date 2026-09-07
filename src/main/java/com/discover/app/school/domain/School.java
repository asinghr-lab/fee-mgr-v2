package com.discover.app.school.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "schools")
public class School {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 150)
	private String name;
	@Column(length = 255)
	private String address;
	@Column(length = 30)
	private String phoneNumber;
	@Column(length = 150)
	private String email;
	@Column(name = "fee_due_day", nullable = false)
	private Integer feeDueDay = 10;

	protected School() {
	}

	public School(String name, String address, String phoneNumber, String email) {
		this(name, address, phoneNumber, email, 10);
	}

	public School(String name, String address, String phoneNumber, String email, Integer feeDueDay) {
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.feeDueDay = validateDueDay(feeDueDay);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public Integer getFeeDueDay() {
		return feeDueDay;
	}

	public void update(String name, String address, String phoneNumber, String email, Integer feeDueDay) {
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.feeDueDay = validateDueDay(feeDueDay);
	}

	private static int validateDueDay(Integer day) {
		if (day == null || day < 1 || day > 28)
			throw new IllegalArgumentException("Fee due day must be between 1 and 28.");
		return day;
	}
}
