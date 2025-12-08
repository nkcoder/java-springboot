package org.nkcoder.user.interfaces.rest;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nkcoder.shared.local.rest.ApiResponse;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.application.service.UserCommandService;
import org.nkcoder.user.application.service.UserQueryService;
import org.nkcoder.user.interfaces.rest.mapper.UserRequestMapper;
import org.nkcoder.user.interfaces.rest.request.AdminResetPasswordRequest;
import org.nkcoder.user.interfaces.rest.request.AdminUpdateUserRequest;
import org.nkcoder.user.interfaces.rest.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for admin user management operations. */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserQueryService queryService;
    private final UserCommandService commandService;
    private final UserRequestMapper requestMapper;

    public AdminUserController(
            UserQueryService queryService, UserCommandService commandService, UserRequestMapper requestMapper) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        logger.debug("Admin getting all users");

        List<UserResponse> users =
                queryService.getAllUsers().stream().map(UserResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        logger.debug("Admin getting user: {}", userId);

        UserDto user = queryService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("User retrieved", UserResponse.from(user)));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        logger.info("Admin updating user: {}", userId);

        UserDto user = commandService.adminUpdateUser(requestMapper.toCommand(userId, request));

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", UserResponse.from(user)));
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID userId, @Valid @RequestBody AdminResetPasswordRequest request) {
        logger.info("Admin resetting password for user: {}", userId);

        commandService.adminResetPassword(requestMapper.toCommand(userId, request));

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
