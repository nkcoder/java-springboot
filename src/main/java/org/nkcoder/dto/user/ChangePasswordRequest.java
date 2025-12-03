package org.nkcoder.dto.user;

import static org.nkcoder.validation.ValidationMessages.CONFIRM_PASSWORD_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.CURRENT_PASSWORD_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_COMPLEXITY;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_COMPLEXITY_PATTERN;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_MIN_LENGTH;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_SIZE;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.validation.PasswordMatch;

@PasswordMatch
public record ChangePasswordRequest(
    @NotBlank(message = CURRENT_PASSWORD_REQUIRED) String currentPassword,
    @NotBlank(message = PASSWORD_REQUIRED) @Size(min = PASSWORD_MIN_LENGTH, message = PASSWORD_SIZE) @Pattern(regexp = PASSWORD_COMPLEXITY_PATTERN, message = PASSWORD_COMPLEXITY) String newPassword,
    @NotBlank(message = CONFIRM_PASSWORD_REQUIRED) String confirmPassword) {}
