package org.nkcoder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_tokens_token", columnList = "token"),
      @Index(name = "idx_refresh_tokens_token_family", columnList = "token_family"),
      @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
    })
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private final String token;

  @Column(name = "token_family", nullable = false)
  private final String tokenFamily;

  @Column(name = "user_id", nullable = false)
  private final UUID userId;

  @Column(name = "expires_at", nullable = false)
  private final LocalDateTime expiresAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  // JPA requires a no-arg constructor, but we need to initialize the final fields
  // `protected` signals this is for JPA only, not application code, we initialize to null because
  // JPA will override via reflection.
  // This is a standard JPA pattern for immutable entities
  protected RefreshToken() {
    this.token = null;
    this.tokenFamily = null;
    this.userId = null;
    this.expiresAt = null;
  }

  public RefreshToken(String token, String tokenFamily, UUID userId, LocalDateTime expiresAt) {
    this.token = Objects.requireNonNull(token, "token must not be null");
    this.tokenFamily = Objects.requireNonNull(tokenFamily, "tokenFamily must not be null");
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public String getTokenFamily() {
    return tokenFamily;
  }

  public UUID getUserId() {
    return userId;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public User getUser() {
    return user;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  @Override
  public String toString() {
    String maskedToken =
        token != null && token.length() > 8
            ? token.substring(0, 4) + "..." + token.substring(token.length() - 4)
            : "****";
    return "RefreshToken{"
        + "id="
        + id
        + ", token='"
        + maskedToken
        + '\''
        + ", tokenFamily='"
        + tokenFamily
        + '\''
        + ", userId="
        + userId
        + ", expiresAt="
        + expiresAt
        + ", createdAt="
        + createdAt
        + '}';
  }
}
