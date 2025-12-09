package org.nkcoder.user.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import org.nkcoder.shared.kernel.domain.valueobject.AggregateRoot;
import org.nkcoder.user.domain.event.UserProfileUpdatedEvent;

/**
 * User aggregate root. Unified domain model combining authentication and profile concerns. This is the single source of
 * truth for user identity, credentials, and profile data.
 */
public class User extends AggregateRoot<UserId> {

    private final UserId id;
    private Email email;
    private HashedPassword password;
    private UserName name;
    private final UserRole role;
    private boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(
            UserId id,
            Email email,
            HashedPassword password,
            UserName name,
            UserRole role,
            boolean emailVerified,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.role = role != null ? role : UserRole.MEMBER;
        this.emailVerified = emailVerified;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory method for creating a new user during registration. */
    public static User register(Email email, HashedPassword password, UserName name, UserRole role) {
        LocalDateTime now = LocalDateTime.now();
        return new User(UserId.generate(), email, password, name, role, false, null, now, now);
    }

    /** Factory method for reconstituting from persistence. */
    public static User reconstitute(
            UserId id,
            Email email,
            HashedPassword password,
            UserName name,
            UserRole role,
            boolean emailVerified,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new User(id, email, password, name, role, emailVerified, lastLoginAt, createdAt, updatedAt);
    }

    /** Records the current time as the last login time. */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Changes the password to a new hashed password. */
    public void changePassword(HashedPassword newPassword) {
        this.password = Objects.requireNonNull(newPassword, "New password cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    /** Updates the user's profile information. Registers a domain event for the change. */
    public void updateProfile(UserName newName) {
        UserName oldName = this.name;
        this.name = Objects.requireNonNull(newName, "Name cannot be null");
        this.updatedAt = LocalDateTime.now();

        registerEvent(new UserProfileUpdatedEvent(this.id, oldName, newName));
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

    // Getters

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public HashedPassword getPassword() {
        return password;
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
