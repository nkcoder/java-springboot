package org.nkcoder.mapper;

import java.util.Objects;
import java.util.Optional;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  /** Converts a User entity to UserResponse, returning Optional.empty() is user is null. */
  public Optional<UserResponse> toResponse(User user) {
    return Optional.ofNullable(user).map(this::mapToResponse);
  }

  public UserResponse toResponseOrThrow(User user) {
    Objects.requireNonNull(user, "User must not be null");
    return mapToResponse(user);
  }

  private UserResponse mapToResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.emailVerified(),
        user.getLastLoginAt(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
