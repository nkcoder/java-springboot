package org.nkcoder.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a token family. Token families are used to track related refresh tokens across rotations,
 * enabling multi-device logout.
 */
public record TokenFamily(String value) {

    public TokenFamily {
        Objects.requireNonNull(value, "TokenFamily value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TokenFamily value cannot be blank");
        }
    }

    public static TokenFamily generate() {
        return new TokenFamily(UUID.randomUUID().toString());
    }

    public static TokenFamily of(String value) {
        return new TokenFamily(value);
    }
}
