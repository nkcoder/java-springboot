package org.nkcoder.dto.auth;

import static org.nkcoder.validation.ValidationMessages.EMAIL_INVALID;
import static org.nkcoder.validation.ValidationMessages.EMAIL_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.NAME_MAX_LENGTH;
import static org.nkcoder.validation.ValidationMessages.NAME_MIN_LENGTH;
import static org.nkcoder.validation.ValidationMessages.NAME_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.NAME_SIZE;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_COMPLEXITY;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_COMPLEXITY_PATTERN;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_MIN_LENGTH;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_REQUIRED;
import static org.nkcoder.validation.ValidationMessages.PASSWORD_SIZE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.enums.Role;

public record RegisterRequest(
    @NotBlank(message = EMAIL_REQUIRED) @Email(message = EMAIL_INVALID) String email,
    @NotBlank(message = PASSWORD_REQUIRED) @Size(min = PASSWORD_MIN_LENGTH, message = PASSWORD_SIZE) @Pattern(regexp = PASSWORD_COMPLEXITY_PATTERN, message = PASSWORD_COMPLEXITY) String password,
    @NotBlank(message = NAME_REQUIRED) @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH, message = NAME_SIZE) String name,
    Role role) {
  // Compact constructor that normalizes email to lowercase
  public RegisterRequest {
    if (email != null) {
      email = email.toLowerCase().trim();
    }
    if (name != null) {
      name = name.trim();
    }
  }
}
