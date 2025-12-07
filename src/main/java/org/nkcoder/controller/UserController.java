package org.nkcoder.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.nkcoder.annotation.CurrentUser;
import org.nkcoder.dto.common.ApiResponse;
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ApiResponse<UserResponse>> getMe(@CurrentUser UUID userId) {
    logger.info("Get profile request for userId: {}", userId);

    UserResponse userResponse = userService.findById(userId);

    return ResponseEntity.ok(
        ApiResponse.success("User profile retrieved successfully", userResponse));
  }

  @PatchMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> updateMe(
      @CurrentUser UUID userId, @Valid @RequestBody UpdateProfileRequest request) {

    logger.info("Update profile request for userId: {}", userId);

    UserResponse userResponse = userService.updateProfile(userId, request);

    return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userResponse));
  }

  @PatchMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> changeMyPassword(
      @CurrentUser UUID userId, @Valid @RequestBody ChangePasswordRequest request) {

    logger.info("Change password request for userId: {}", userId);

    userService.changePassword(userId, request);

    return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
  }
}
