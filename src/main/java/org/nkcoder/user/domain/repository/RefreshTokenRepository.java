package org.nkcoder.user.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.UserId;

/** Repository interface (port) for RefreshToken persistence. */
public interface RefreshTokenRepository {

    /** Finds a refresh token by its value. */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds a refresh token exclusively for update. Used during token refresh to prevent race conditions.
     * Implementation should use pessimistic locking.
     */
    Optional<RefreshToken> findByTokenExclusively(String token);

    /** Saves a refresh token. */
    RefreshToken save(RefreshToken refreshToken);

    /** Deletes a refresh token by its value. */
    void deleteByToken(String token);

    /** Deletes all refresh tokens in a token family (logout from all devices). */
    void deleteByTokenFamily(TokenFamily tokenFamily);

    /** Deletes all refresh tokens for a user. */
    void deleteByUserId(UserId userId);

    /** Deletes all expired refresh tokens. */
    void deleteExpiredTokens(LocalDateTime now);
}
