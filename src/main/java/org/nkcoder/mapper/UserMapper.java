package org.nkcoder.mapper;

import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    if (user == null) {
      return null;
    }

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
