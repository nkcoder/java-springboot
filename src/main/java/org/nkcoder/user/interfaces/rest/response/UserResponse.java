package org.nkcoder.user.interfaces.rest.response;

import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.user.application.dto.response.UserDto;

/** REST API response for user information. */
public record UserResponse(
    UUID id,
    String email,
    String name,
    String role,
    boolean emailVerified,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static UserResponse from(UserDto dto) {
    return new UserResponse(
        dto.id(),
        dto.email(),
        dto.name(),
        dto.role(),
        dto.emailVerified(),
        dto.lastLoginAt(),
        dto.createdAt(),
        dto.updatedAt());
  }
}
