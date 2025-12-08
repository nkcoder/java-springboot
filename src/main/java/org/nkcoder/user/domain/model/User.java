package org.nkcoder.user.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.nkcoder.user.domain.event.UserProfileUpdatedEvent;

/**
 * User aggregate root in the User bounded context. Represents a user's profile and identity
 * information.
 */
public class User {

  private final UserId id;
  private Email email;
  private UserName name;
  private final UserRole role;
  private boolean emailVerified;
  private LocalDateTime lastLoginAt;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private User(
      UserId id,
      Email email,
      UserName name,
      UserRole role,
      boolean emailVerified,
      LocalDateTime lastLoginAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = Objects.requireNonNull(id, "User ID cannot be null");
    this.email = Objects.requireNonNull(email, "Email cannot be null");
    this.name = Objects.requireNonNull(name, "Name cannot be null");
    this.role = Objects.requireNonNull(role, "Role cannot be null");
    this.emailVerified = emailVerified;
    this.lastLoginAt = lastLoginAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Creates a new user (typically from registration in Auth context). */
  public static User create(UserId id, Email email, UserName name, UserRole role) {
    LocalDateTime now = LocalDateTime.now();
    return new User(id, email, name, role, false, null, now, now);
  }

  /** Reconstitutes a User from persistence. */
  public static User reconstitute(
      UserId id,
      Email email,
      UserName name,
      UserRole role,
      boolean emailVerified,
      LocalDateTime lastLoginAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    return new User(id, email, name, role, emailVerified, lastLoginAt, createdAt, updatedAt);
  }

  /** Updates the user's profile information. */
  public UserProfileUpdatedEvent updateProfile(UserName newName) {
    UserName oldName = this.name;
    this.name = Objects.requireNonNull(newName, "Name cannot be null");
    this.updatedAt = LocalDateTime.now();

    return new UserProfileUpdatedEvent(this.id, oldName, newName);
  }

  /** Updates the user's email address. */
  public void updateEmail(Email newEmail) {
    this.email = Objects.requireNonNull(newEmail, "Email cannot be null");
    this.emailVerified = false;
    this.updatedAt = LocalDateTime.now();
  }

  /** Marks the email as verified. */
  public void verifyEmail() {
    this.emailVerified = true;
    this.updatedAt = LocalDateTime.now();
  }

  /** Records a login event (called when Auth context notifies of login). */
  public void recordLogin() {
    this.lastLoginAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // Getters

  public UserId getId() {
    return id;
  }

  public Email getEmail() {
    return email;
  }

  public UserName getName() {
    return name;
  }

  public UserRole getRole() {
    return role;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isAdmin() {
    return role == UserRole.ADMIN;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
