package org.nkcoder.user.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request for updating user profile. */
public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters") String name) {}
