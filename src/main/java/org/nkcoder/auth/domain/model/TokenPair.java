package org.nkcoder.auth.domain.model;

import java.util.Objects;

/** Value object representing a pair of access and refresh tokens. */
public record TokenPair(String accessToken, String refreshToken) {

    public TokenPair {
        Objects.requireNonNull(accessToken, "Access token cannot be null");
        Objects.requireNonNull(refreshToken, "Refresh token cannot be null");
    }
}
