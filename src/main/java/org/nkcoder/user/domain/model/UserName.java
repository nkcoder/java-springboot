package org.nkcoder.user.domain.model;

import java.util.Objects;
import org.nkcoder.shared.kernel.exception.ValidationException;

/** Value object representing a user's display name. */
public record UserName(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;

    public UserName {
        Objects.requireNonNull(value, "Name cannot be null");
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Name must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH));
        }
    }

    public static UserName of(String value) {
        return new UserName(value.trim());
    }

    @Override
    public String value() {
        return value.trim();
    }

    @Override
    public String toString() {
        return value();
    }
}
