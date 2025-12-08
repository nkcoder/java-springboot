package org.nkcoder.user.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.user.domain.model.User;

/** DTO representing user information. */
public record UserDto(
    UUID id,
    String email,
    String name,
    String role,
    boolean emailVerified,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static UserDto from(User user) {
    return new UserDto(
        user.getId().value(),
        user.getEmail().value(),
        user.getName().value(),
        user.getRole().name(),
        user.isEmailVerified(),
        user.getLastLoginAt(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
