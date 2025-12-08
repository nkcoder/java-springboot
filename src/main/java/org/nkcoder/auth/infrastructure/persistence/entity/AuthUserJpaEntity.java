package org.nkcoder.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.auth.domain.model.AuthRole;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity for users table, mapped for Auth context. Contains only authentication-related fields.
 * Implements Persistable to control new/existing entity detection since we provide our own UUID.
 */
@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_users_email", columnList = "email")})
@EntityListeners(AuditingEntityListener.class)
public class AuthUserJpaEntity implements Persistable<UUID> {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthRole role;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Transient private boolean isNew = false;

  // Required by JPA
  protected AuthUserJpaEntity() {}

  public AuthUserJpaEntity(
      UUID id,
      String email,
      String password,
      String name,
      AuthRole role,
      LocalDateTime lastLoginAt) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.name = name;
    this.role = role;
    this.lastLoginAt = lastLoginAt;
  }

  // Getters and setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthRole getRole() {
    return role;
  }

  public void setRole(AuthRole role) {
    this.role = role;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
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
