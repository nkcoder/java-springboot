package org.nkcoder.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.enums.Role;

public record UserResponse(
        UUID id,
        String email,
        String name,
        Role role,
        Boolean isEmailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
