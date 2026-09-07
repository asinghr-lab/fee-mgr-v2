package com.discover.app.identity.web;

import com.discover.app.identity.domain.Role;
import com.discover.app.identity.dto.IdentityDtos.*;
import com.discover.app.identity.service.IdentityAdminService;
import com.discover.app.identity.service.IdentityUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IdentityWebController {
	private final IdentityUserService userService;
	private final IdentityAdminService adminService;

	public IdentityWebController(IdentityUserService userService, IdentityAdminService adminService) {
		this.userService = userService;
		this.adminService = adminService;
	}

	@GetMapping("/login")
	public String login() {
		return "identity/login";
	}

	@GetMapping("/")
	public String home(Authentication authentication) {
		boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		return admin ? "redirect:/admin-dashboard" : "redirect:/staff-dashboard";
	}

	@GetMapping("/admin-dashboard")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminDashboard(Model model, Authentication authentication) {
		model.addAttribute("username", authentication.getName());
		return "identity/admin-dashboard";
	}

	@GetMapping("/staff-dashboard")
	@PreAuthorize("hasRole('STAFF')")
	public String staffDashboard(Model model, Authentication authentication) {
		model.addAttribute("username", authentication.getName());
		return "identity/staff-dashboard";
	}

	@GetMapping("/profile")
	public String profile(Model model, Authentication authentication) {
		var user = userService.current(authentication.getName());
		var profile = adminService.profile(user.getId());
		model.addAttribute("profileRequest",
				new ProfileRequest(profile.getDisplayName() == null ? "" : profile.getDisplayName()));
		model.addAttribute("profileUser", user);
		model.addAttribute("profileRoles",
				user.getUserRoles().stream().map(ur -> ur.getRole().name()).sorted().toList());
		return "identity/profile";
	}

	@PostMapping("/profile")
	public String updateProfile(@Valid @ModelAttribute("profileRequest") ProfileRequest request,
			BindingResult bindingResult, Authentication authentication, Model model, RedirectAttributes redirect) {
		var user = userService.current(authentication.getName());
		if (bindingResult.hasErrors()) {
			model.addAttribute("profileUser", user);
			model.addAttribute("profileRoles",
					user.getUserRoles().stream().map(ur -> ur.getRole().name()).sorted().toList());
			return "identity/profile";
		}
		userService.updateProfile(authentication.getName(), request.displayName());
		redirect.addFlashAttribute("message", "Profile updated successfully.");
		return "redirect:/profile";
	}

	@GetMapping("/reset-password")
	public String resetPassword(Model model) {
		model.addAttribute("request", new PasswordResetRequest("", "", ""));
		return "identity/reset-password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@Valid @ModelAttribute("request") PasswordResetRequest request,
			BindingResult bindingResult, Authentication authentication, RedirectAttributes redirect) {
		if (bindingResult.hasErrors())
			return "identity/reset-password";
		try {
			userService.resetPassword(authentication.getName(), request);
			redirect.addFlashAttribute("message", "Password updated successfully.");
			return "redirect:/profile";
		} catch (IllegalArgumentException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
			return "redirect:/reset-password";
		}
	}

	@GetMapping("/admin/users")
	@PreAuthorize("hasRole('ADMIN')")
	public String users(@RequestParam(defaultValue = "0") int page, Model model) {
		int safePage = Math.max(page, 0);
		var result = adminService.findUsers(PageRequest.of(safePage, 10));
		model.addAttribute("users", result);
		return "identity/users";
	}

	@GetMapping("/admin/users/new")
	@PreAuthorize("hasRole('ADMIN')")
	public String newUser(Model model) {
		model.addAttribute("request", new UserRequest("", "", "", Role.STAFF, ""));
		model.addAttribute("roles", Role.values());
		return "identity/user-form";
	}

	@PostMapping("/admin/users")
	@PreAuthorize("hasRole('ADMIN')")
	public String createUser(@Valid @ModelAttribute("request") UserRequest request, BindingResult bindingResult,
			Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("roles", Role.values());
			return "identity/user-form";
		}
		try {
			if (!request.password().equals(request.confirmPassword()))
				throw new IllegalArgumentException("Password and confirmation do not match.");
			adminService.create(request);
			redirect.addFlashAttribute("message", "User created successfully.");
			return "redirect:/admin/users";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("roles", Role.values());
			model.addAttribute("error", ex.getMessage());
			return "identity/user-form";
		}
	}

	@GetMapping("/admin/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String userProfile(@PathVariable Long id, Model model) {
		var user = adminService.findUser(id);
		var profile = adminService.profile(id);
		model.addAttribute("profileUser", user);
		model.addAttribute("userProfile", profile);
		model.addAttribute("passwordRequest", new AdminPasswordRequest("", ""));
		return "identity/admin-user-profile";
	}

	@PostMapping("/admin/users/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public String changeStatus(@PathVariable Long id, @RequestParam boolean enabled, Authentication authentication,
			RedirectAttributes redirect) {
		try {
			adminService.setEnabled(id, enabled, authentication.getName());
			redirect.addFlashAttribute("message", enabled ? "User activated." : "User deactivated.");
		} catch (IllegalStateException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/admin/users/" + id;
	}

	@PostMapping("/admin/users/{id}/password")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminResetPassword(@PathVariable Long id,
			@Valid @ModelAttribute("passwordRequest") AdminPasswordRequest request, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			redirect.addFlashAttribute("error",
					"Password must be at least 8 characters and both entries must be provided.");
			return "redirect:/admin/users/" + id;
		}
		if (!request.newPassword().equals(request.confirmPassword())) {
			redirect.addFlashAttribute("error", "New password and confirmation do not match.");
			return "redirect:/admin/users/" + id;
		}
		adminService.resetPassword(id, request.newPassword());
		redirect.addFlashAttribute("message", "User password has been reset.");
		return "redirect:/admin/users/" + id;
	}
}
