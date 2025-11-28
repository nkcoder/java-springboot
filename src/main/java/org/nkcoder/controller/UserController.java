package org.nkcoder.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.nkcoder.dto.common.ApiResponse;
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");

        logger.info("Get profile request for user: {}", email);

        UserResponse userResponse = userService.findById(userId);

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @Valid @RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {

        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String email = (String) httpRequest.getAttribute("email");

        logger.info("Update profile request for user: {}", email);

        UserResponse userResponse = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userResponse));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {

        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String email = (String) httpRequest.getAttribute("email");

        logger.info("Change password request for user: {}", email);

        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // Admin endpoints
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        logger.info("Admin get user request for user: {}", userId);

        UserResponse userResponse = userService.findById(userId);

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request) {

        logger.info("Admin update user request for user: {}", userId);

        UserResponse userResponse = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
    }

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changeUserPassword(
            @PathVariable UUID userId, @RequestBody ChangePasswordRequest request) {

        logger.info("Admin change password request for user: {}", userId);

        // For admin, we only use the newPassword field
        userService.changeUserPassword(userId, request.newPassword());

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
