package org.nkcoder.auth.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.model.RefreshToken;
import org.nkcoder.auth.domain.model.TokenFamily;

/**
 * Repository interface (port) for RefreshToken persistence. Implementations are in the
 * infrastructure layer.
 */
public interface RefreshTokenRepository {

  Optional<RefreshToken> findByToken(String token);

  /**
   * Finds a refresh token by its value with a pessimistic lock for update. Used during token
   * refresh to prevent race conditions.
   */
  Optional<RefreshToken> findByTokenForUpdate(String token);

  RefreshToken save(RefreshToken refreshToken);

  void deleteByToken(String token);

  void deleteByTokenFamily(TokenFamily tokenFamily);

  void deleteByUserId(AuthUserId userId);

  void deleteExpiredTokens(LocalDateTime now);
}
