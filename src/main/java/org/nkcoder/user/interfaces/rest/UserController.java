package org.nkcoder.user.interfaces.rest;

import jakarta.validation.Valid;
import java.util.UUID;
import org.nkcoder.shared.local.rest.ApiResponse;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.application.service.UserCommandService;
import org.nkcoder.user.application.service.UserQueryService;
import org.nkcoder.user.interfaces.rest.mapper.UserRequestMapper;
import org.nkcoder.user.interfaces.rest.request.ChangePasswordRequest;
import org.nkcoder.user.interfaces.rest.request.UpdateProfileRequest;
import org.nkcoder.user.interfaces.rest.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for user profile operations. */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserQueryService queryService;
  private final UserCommandService commandService;
  private final UserRequestMapper requestMapper;

  public UserController(
      UserQueryService queryService,
      UserCommandService commandService,
      UserRequestMapper requestMapper) {
    this.queryService = queryService;
    this.commandService = commandService;
    this.requestMapper = requestMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @RequestAttribute("userId") UUID userId) {
    logger.debug("Getting current user profile");

    UserDto user = queryService.getUserById(userId);

    return ResponseEntity.ok(
        ApiResponse.success("User profile retrieved", UserResponse.from(user)));
  }

  @PatchMapping
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      @RequestAttribute("userId") UUID userId, @Valid @RequestBody UpdateProfileRequest request) {
    logger.info("Updating profile for user: {}", userId);

    UserDto user = commandService.updateProfile(requestMapper.toCommand(userId, request));

    return ResponseEntity.ok(
        ApiResponse.success("Profile updated successfully", UserResponse.from(user)));
  }

  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @RequestAttribute("userId") UUID userId, @Valid @RequestBody ChangePasswordRequest request) {
    logger.info("Changing password for user: {}", userId);

    commandService.changePassword(requestMapper.toCommand(userId, request));

    return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
  }
}
