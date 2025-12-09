package org.nkcoder.user.interfaces.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email") String email,

        @NotBlank(message = "Password is required") String password) {

    public LoginRequest {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
