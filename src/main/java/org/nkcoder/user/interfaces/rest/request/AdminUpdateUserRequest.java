package org.nkcoder.user.interfaces.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Request for admin updating a user. */
public record AdminUpdateUserRequest(
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters") String name,

        @Email(message = "Invalid email format") String email) {}
