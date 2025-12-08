package org.nkcoder.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Value object representing a user's unique identifier in the Auth context. */
public record AuthUserId(UUID value) {

    public AuthUserId {
        Objects.requireNonNull(value, "AuthUserId value cannot be null");
    }

    public static AuthUserId generate() {
        return new AuthUserId(UUID.randomUUID());
    }

    public static AuthUserId of(UUID value) {
        return new AuthUserId(value);
    }

    public static AuthUserId of(String value) {
        return new AuthUserId(UUID.fromString(value));
    }
}
