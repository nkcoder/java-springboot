package org.nkcoder.auth.interfaces.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nkcoder.auth.domain.model.AuthRole;

public record RegisterRequest(
    @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email") String email,
    @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message =
                "Password must contain at least one lowercase letter, one uppercase letter, and one"
                    + " number")
        String password,
    @NotBlank(message = "Name is required") @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String name,
    AuthRole role) {

  public RegisterRequest {
    if (email != null) {
      email = email.toLowerCase().trim();
    }
    if (name != null) {
      name = name.trim();
    }
  }
}
