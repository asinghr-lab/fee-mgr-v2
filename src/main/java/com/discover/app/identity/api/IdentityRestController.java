package com.discover.app.identity.api;

import com.discover.app.identity.dto.IdentityDtos.*;
import com.discover.app.identity.repository.*;
import com.discover.app.identity.service.IdentityAdminService;
import com.discover.app.identity.service.IdentityUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class IdentityRestController {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final IdentityUserService userService;
    private final IdentityAdminService adminService;

    public IdentityRestController(UserRepository users, UserProfileRepository profiles,
            IdentityUserService userService, IdentityAdminService adminService) {
        this.users = users;
        this.profiles = profiles;
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        var user = users.findByUsername(authentication.getName()).orElseThrow();
        var profile = profiles.findById(user.getId()).orElse(null);
        return new CurrentUserResponse(user.getUsername(), profile == null ? null : profile.getDisplayName(),
                user.getUserRoles().stream().map(r -> r.getRole().name())
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    @PutMapping("/me/profile")
    public void updateMyProfile(@Valid @RequestBody ProfileRequest request, Authentication authentication) {
        userService.updateProfile(authentication.getName(), request.displayName());
    }

    @PostMapping("/me/password")
    public void resetMyPassword(@Valid @RequestBody PasswordResetRequest request, Authentication authentication) {
        userService.resetPassword(authentication.getName(), request);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.data.domain.Page<?> users(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        return adminService.findUsers(PageRequest.of(Math.max(page, 0), safeSize))
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.isEnabled(),
                        u.getUserRoles().stream().map(r -> r.getRole().name()).toList()));
    }

    public record UserSummary(Long id, String username, boolean enabled, java.util.List<String> roles) {
    }
}
