package com.discover.app.identity.service;

import com.discover.app.identity.domain.*;
import com.discover.app.identity.domain.Role;
import com.discover.app.identity.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class IdentityDataInitializer {
	@Bean
	CommandLineRunner initializeIdentity(UserRepository users, UserProfileRepository profiles,
			PasswordEncoder encoder) {
		return args -> {
			if (users.findByUsername("admin").isEmpty()) {
				User u = new User("admin", encoder.encode("admin123"), true);
				u.addRole(Role.ADMIN);
				u.addRole(Role.STAFF);
				users.save(u);
				profiles.save(new UserProfile(u, "Administrator"));
			}
			if (users.findByUsername("staff").isEmpty()) {
				User u = new User("staff", encoder.encode("staff123"), true);
				u.addRole(Role.STAFF);
				users.save(u);
				profiles.save(new UserProfile(u, "Staff User"));
			}
		};
	}
}
