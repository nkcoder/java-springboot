package org.nkcoder.user.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a refresh token. Refresh tokens are used to obtain new access tokens without re-authentication.
 * They belong to a token family for multi-device logout support.
 */
public class RefreshToken {

    private final UUID id;
    private final String token;
    private final TokenFamily tokenFamily;
    private final UserId userId;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    private RefreshToken(
            UUID id,
            String token,
            TokenFamily tokenFamily,
            UserId userId,
            LocalDateTime expiresAt,
            LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.token = Objects.requireNonNull(token, "token cannot be null");
        this.tokenFamily = Objects.requireNonNull(tokenFamily, "tokenFamily cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    /** Factory method for creating a new refresh token. */
    public static RefreshToken create(String token, TokenFamily tokenFamily, UserId userId, LocalDateTime expiresAt) {
        return new RefreshToken(UUID.randomUUID(), token, tokenFamily, userId, expiresAt, LocalDateTime.now());
    }

    /** Factory method for reconstituting from persistence. */
    public static RefreshToken reconstitute(
            UUID id,
            String token,
            TokenFamily tokenFamily,
            UserId userId,
            LocalDateTime expiresAt,
            LocalDateTime createdAt) {
        return new RefreshToken(id, token, tokenFamily, userId, expiresAt, createdAt);
    }

    /** Checks if this token has expired. */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public TokenFamily getTokenFamily() {
        return tokenFamily;
    }

    public UserId getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
