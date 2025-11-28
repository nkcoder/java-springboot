package org.nkcoder.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email(message = "Please provide a valid email") String email,
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String name) {}
