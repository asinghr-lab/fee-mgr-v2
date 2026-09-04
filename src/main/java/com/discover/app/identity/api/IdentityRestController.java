package com.discover.app.identity.api;

import com.discover.app.identity.dto.IdentityDtos.CurrentUserResponse;
import com.discover.app.identity.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class IdentityRestController {
    private final UserRepository users;
    private final UserProfileRepository profiles;

    public IdentityRestController(UserRepository users, UserProfileRepository profiles) {
        this.users = users; this.profiles = profiles;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        var user = users.findByUsername(authentication.getName()).orElseThrow();
        var profile = profiles.findById(user.getId()).orElse(null);
        return new CurrentUserResponse(
            user.getUsername(),
            profile == null ? null : profile.getDisplayName(),
            user.getUserRoles().stream().map(r -> r.getRole().name())
                .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }
}
