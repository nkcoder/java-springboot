package org.nkcoder.dto.user;

import static org.nkcoder.validation.ValidationMessages.EMAIL_INVALID;
import static org.nkcoder.validation.ValidationMessages.NAME_MAX_LENGTH;
import static org.nkcoder.validation.ValidationMessages.NAME_MIN_LENGTH;
import static org.nkcoder.validation.ValidationMessages.NAME_SIZE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Email(message = EMAIL_INVALID) String email,
    @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH, message = NAME_SIZE) String name) {}
