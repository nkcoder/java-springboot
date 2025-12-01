package org.nkcoder.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshTokenFactory {

  private RefreshTokenFactory() {}

  public static RefreshTokenBuilder aToken() {
    return new RefreshTokenBuilder();
  }

  public static RefreshTokenBuilder anExpiredToken() {
    return new RefreshTokenBuilder().withExpiresAt(LocalDateTime.now().minusDays(1));
  }

  public static final class RefreshTokenBuilder {
    private String token = "test-token-" + UUID.randomUUID();
    private String tokenFamily = UUID.randomUUID().toString();
    private UUID userId = UUID.randomUUID();
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

    private RefreshTokenBuilder() {}

    public RefreshTokenBuilder withToken(String token) {
      this.token = token;
      return this;
    }

    public RefreshTokenBuilder withTokenFamily(String tokenFamily) {
      this.tokenFamily = tokenFamily;
      return this;
    }

    public RefreshTokenBuilder forUser(UUID userId) {
      this.userId = userId;
      return this;
    }

    public RefreshTokenBuilder withExpiresAt(LocalDateTime expiresAt) {
      this.expiresAt = expiresAt;
      return this;
    }

    public RefreshToken build() {
      return new RefreshToken(token, tokenFamily, userId, expiresAt);
    }
  }
}
