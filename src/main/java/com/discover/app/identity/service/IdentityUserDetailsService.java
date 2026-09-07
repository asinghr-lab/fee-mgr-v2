package com.discover.app.identity.service;

import com.discover.app.identity.domain.User;
import com.discover.app.identity.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	public IdentityUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		var authorities = user.getUserRoles().stream()
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole().name())).toList();
		return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
				.password(user.getPassword()).disabled(!user.isEnabled()).authorities(authorities).build();
	}
}
