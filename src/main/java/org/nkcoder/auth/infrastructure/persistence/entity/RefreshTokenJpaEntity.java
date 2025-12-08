package org.nkcoder.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity for refresh_tokens table. Implements Persistable to control new/existing entity
 * detection since we provide our own UUID.
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_tokens_token", columnList = "token"),
      @Index(name = "idx_refresh_tokens_token_family", columnList = "token_family"),
      @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
    })
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenJpaEntity implements Persistable<UUID> {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "token_family", nullable = false)
  private String tokenFamily;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Transient private boolean isNew = false;

  // Required by JPA
  protected RefreshTokenJpaEntity() {}

  public RefreshTokenJpaEntity(
      UUID id,
      String token,
      String tokenFamily,
      UUID userId,
      LocalDateTime expiresAt,
      LocalDateTime createdAt) {
    this.id = id;
    this.token = token;
    this.tokenFamily = tokenFamily;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  // Getters and setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenFamily() {
    return tokenFamily;
  }

  public void setTokenFamily(String tokenFamily) {
    this.tokenFamily = tokenFamily;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  // Persistable implementation

  @Override
  public boolean isNew() {
    return isNew;
  }

  public void markAsNew() {
    this.isNew = true;
  }
}
