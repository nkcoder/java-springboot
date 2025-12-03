package org.nkcoder.dto.auth;

import static org.nkcoder.validation.ValidationMessages.EMAIL_INVALID;
import static org.nkcoder.validation.ValidationMessages.EMAIL_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_REQUIRED;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = EMAIL_REQUIRED) @Email(message = EMAIL_INVALID) String email,
    @NotBlank(message = PASSWORD_REQUIRED) String password) {}
