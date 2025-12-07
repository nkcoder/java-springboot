package org.nkcoder.controller;

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
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
    logger.info("Admin get user request for userId: {}", userId);

    UserResponse userResponse = userService.findById(userId);

    return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request) {

    logger.info("Admin update user request for userId: {}", userId);

    UserResponse userResponse = userService.updateProfile(userId, request);

    return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
  }

  @PatchMapping("/{userId}/password")
  public ResponseEntity<ApiResponse<Void>> changeUserPassword(
      @PathVariable UUID userId, @Valid @RequestBody ChangePasswordRequest request) {

    logger.info("Admin change password request for userId: {}", userId);

    // For admin, we only use the newPassword field
    userService.changeUserPassword(userId, request.newPassword());

    return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
  }
}
