package org.nkcoder.shared.kernel.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/** Email value object with validation. Immutable and self-validating. */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        value = value.toLowerCase().trim();
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
