package org.nkcoder.user.domain.model;

import static java.util.Objects.requireNonNull;

/** Value object representing a hashed password. Never contains raw passwords. */
public record HashedPassword(String value) {

    public HashedPassword {
        requireNonNull(value, "Hashed password cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank");
        }
    }

    public static HashedPassword of(String hashedValue) {
        return new HashedPassword(hashedValue);
    }
}
