package com.discover.app.identity.dto;

import com.discover.app.identity.domain.Role;
import jakarta.validation.constraints.*;
import java.util.Set;

public final class IdentityDtos {
	private IdentityDtos() {
	}

	public record CurrentUserResponse(String username, String displayName, Set<String> roles) {
	}

	public record ProfileRequest(@NotBlank @Size(max = 150) String displayName) {
	}

	public record PasswordResetRequest(@NotBlank String currentPassword,
			@NotBlank @Size(min = 8, max = 100) String newPassword, @NotBlank String confirmPassword) {
	}

	public record AdminPasswordRequest(@NotBlank @Size(min = 8, max = 100) String newPassword,
			@NotBlank String confirmPassword) {
	}

	public record UserRequest(@NotBlank @Size(max = 100) String username,
			@NotBlank @Size(min = 8, max = 100) String password, @NotBlank String confirmPassword, @NotNull Role role,
			@NotBlank @Size(max = 150) String displayName) {
	}
}
