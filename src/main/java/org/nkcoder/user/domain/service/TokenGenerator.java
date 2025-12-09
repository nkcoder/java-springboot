package org.nkcoder.user.domain.service;

import java.time.LocalDateTime;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserRole;

/**
 * Domain service interface for JWT token generation and validation. Implementations are in the infrastructure layer.
 */
public interface TokenGenerator {

    /** Generates an access and refresh token pair. */
    TokenPair generateTokenPair(UserId userId, Email email, UserRole role, TokenFamily tokenFamily);

    /** Returns the expiry time for refresh tokens. */
    LocalDateTime getRefreshTokenExpiry();

    /** Validates an access token and returns its claims. */
    AccessTokenClaims validateAccessToken(String token);

    /** Validates a refresh token and returns its claims. */
    RefreshTokenClaims validateRefreshToken(String token);

    /** Claims extracted from a validated access token. */
    record AccessTokenClaims(UserId userId, Email email, UserRole role) {}

    /** Claims extracted from a validated refresh token. */
    record RefreshTokenClaims(UserId userId, TokenFamily tokenFamily) {}
}
