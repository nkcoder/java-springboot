package org.nkcoder.dto.user;

import static org.nkcoder.validation.PasswordValidation.COMPLEXITY_PATTERN;
import static org.nkcoder.validation.PasswordValidation.CONFIRM_PASSWORD_REQUIRED;
import static org.nkcoder.validation.PasswordValidation.CURRENT_PASSWORD_REQUIRED;
import static org.nkcoder.validation.PasswordValidation.MIN_LENGTH;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_COMPLEXITY;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_MIN_LENGTH;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_REQUIRED;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.validation.PasswordMatch;

@PasswordMatch
public record ChangePasswordRequest(
    @NotBlank(message = CURRENT_PASSWORD_REQUIRED) String currentPassword,
    @NotBlank(message = PASSWORD_REQUIRED) @Size(min = MIN_LENGTH, message = PASSWORD_MIN_LENGTH) @Pattern(regexp = COMPLEXITY_PATTERN, message = PASSWORD_COMPLEXITY) String newPassword,
    @NotBlank(message = CONFIRM_PASSWORD_REQUIRED) String confirmPassword) {}
