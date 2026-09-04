package com.discover.app.identity.service;

import com.discover.app.identity.domain.User;
import com.discover.app.identity.dto.IdentityDtos.PasswordResetRequest;
import com.discover.app.identity.repository.UserProfileRepository;
import com.discover.app.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityUserService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final PasswordEncoder encoder;
    public IdentityUserService(UserRepository users, UserProfileRepository profiles, PasswordEncoder encoder) { this.users=users; this.profiles=profiles; this.encoder=encoder; }

    @Transactional(readOnly=true)
    public User current(String username) { return users.findByUsername(username).orElseThrow(); }

    @Transactional
    public void updateProfile(String username, String displayName) {
        User user = current(username);
        var profile = profiles.findById(user.getId()).orElseGet(() -> new com.discover.app.identity.domain.UserProfile(user, displayName));
        profile.setDisplayName(displayName.trim()); profiles.save(profile);
    }

    @Transactional
    public void resetPassword(String username, PasswordResetRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) throw new IllegalArgumentException("New password and confirmation do not match.");
        User user = current(username);
        if (!encoder.matches(request.currentPassword(), user.getPassword())) throw new IllegalArgumentException("Current password is incorrect.");
        user.setPassword(encoder.encode(request.newPassword())); users.save(user);
    }
}
