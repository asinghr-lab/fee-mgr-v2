package com.discover.app.identity.service;

import com.discover.app.identity.domain.*;
import com.discover.app.identity.dto.IdentityDtos.UserRequest;
import com.discover.app.identity.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityAdminService {
	private final UserRepository users;
	private final UserProfileRepository profiles;
	private final PasswordEncoder encoder;

	public IdentityAdminService(UserRepository users, UserProfileRepository profiles, PasswordEncoder encoder) {
		this.users = users;
		this.profiles = profiles;
		this.encoder = encoder;
	}

	@Transactional(readOnly = true)
	public Page<User> findUsers(Pageable pageable) {
		return users.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public User findUser(Long id) {
		return users.findDetailedById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
	}

	@Transactional(readOnly = true)
	public UserProfile profile(Long id) {
		return profiles.findById(id).orElseThrow(() -> new IllegalArgumentException("User profile not found."));
	}

	@Transactional
	public User create(UserRequest request) {
		if (users.findByUsername(request.username()).isPresent())
			throw new IllegalArgumentException("Username already exists.");
		User user = new User(request.username().trim(), encoder.encode(request.password()), true);
		user.addRole(request.role());
		users.save(user);
		profiles.save(new UserProfile(user, request.displayName().trim()));
		return user;
	}

	@Transactional
	public void updateProfile(Long id, String displayName, Role role) {
		User user = findUser(id);
		user.replaceRoles(role);
		users.save(user);
		UserProfile profile = profiles.findById(id).orElseGet(() -> new UserProfile(user, displayName));
		profile.setDisplayName(displayName.trim());
		profiles.save(profile);
	}

	@Transactional
	public void setEnabled(Long id, boolean enabled, String actingUsername) {
		User user = findUser(id);
		if (user.getUsername().equals(actingUsername) && !enabled)
			throw new IllegalStateException("You cannot deactivate your own account.");
		user.setEnabled(enabled);
		users.save(user);
	}

	@Transactional
	public void resetPassword(Long id, String newPassword) {
		User user = findUser(id);
		user.setPassword(encoder.encode(newPassword));
		users.save(user);
	}
}
