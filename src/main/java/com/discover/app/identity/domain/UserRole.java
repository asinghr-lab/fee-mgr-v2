package com.discover.app.identity.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles", uniqueConstraints = @UniqueConstraint(name = "uk_user_roles_user_role", columnNames = {
		"user_id", "role" }))
public class UserRole {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Role role;

	protected UserRole() {
	}

	public UserRole(User user, Role role) {
		this.user = user;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Role getRole() {
		return role;
	}
}
