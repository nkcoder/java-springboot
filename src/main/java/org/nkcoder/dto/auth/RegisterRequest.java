package org.nkcoder.dto.auth;

import static org.nkcoder.validation.PasswordValidation.COMPLEXITY_PATTERN;
import static org.nkcoder.validation.PasswordValidation.MIN_LENGTH;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_COMPLEXITY;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_MIN_LENGTH;
import static org.nkcoder.validation.PasswordValidation.PASSWORD_REQUIRED;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.enums.Role;

public record RegisterRequest(
    @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email") String email,
    @NotBlank(message = PASSWORD_REQUIRED) @Size(min = MIN_LENGTH, message = PASSWORD_MIN_LENGTH) @Pattern(regexp = COMPLEXITY_PATTERN, message = PASSWORD_COMPLEXITY) String password,
    @NotBlank(message = "Name is required") @Size(min = 2, max = 50, message = "Name must be between 2 and 50 " + "characters") String name,
    Role role) {}
