package org.nkcoder.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required") String currentPassword,
        @NotBlank(message = "New password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                        message =
                                "Password must contain at least one lowercase letter, one uppercase letter, and one number")
                String newPassword,
        @NotBlank(message = "Password confirmation is required") String confirmPassword) {}
