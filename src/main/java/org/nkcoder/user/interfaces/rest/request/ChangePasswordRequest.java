package org.nkcoder.user.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.shared.local.validation.PasswordMatch;

/** Request for changing user's password. */
@PasswordMatch
public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required") String currentPassword,
    @NotBlank(message = "New password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message =
                "Password must contain at least one lowercase letter, one uppercase letter, and one"
                    + " digit")
        String newPassword,
    @NotBlank(message = "Password confirmation is required") String confirmPassword) {}
