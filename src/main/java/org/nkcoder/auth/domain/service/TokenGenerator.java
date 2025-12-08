package org.nkcoder.auth.domain.service;

import java.time.LocalDateTime;
import org.nkcoder.auth.domain.model.AuthRole;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.model.TokenFamily;
import org.nkcoder.auth.domain.model.TokenPair;
import org.nkcoder.shared.kernel.domain.valueobject.Email;

/**
 * Domain service interface for JWT token generation and validation. Implementations are in the
 * infrastructure layer.
 */
public interface TokenGenerator {

  /** Generates an access and refresh token pair. */
  TokenPair generateTokenPair(
      AuthUserId userId, Email email, AuthRole role, TokenFamily tokenFamily);

  /** Returns the expiry time for refresh tokens. */
  LocalDateTime getRefreshTokenExpiry();

  /** Validates a refresh token and returns its claims. */
  RefreshTokenClaims validateRefreshToken(String token);

  /** Claims extracted from a validated refresh token. */
  record RefreshTokenClaims(AuthUserId userId, TokenFamily tokenFamily) {}
}
