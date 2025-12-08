package org.nkcoder.auth.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import org.nkcoder.shared.kernel.domain.valueobject.Email;

/**
 * Auth domain's representation of a user. Contains authentication-related data plus the name (required for DB
 * constraint). This is separate from the User domain's richer user model.
 */
public class AuthUser {

    private final AuthUserId id;
    private final Email email;
    private HashedPassword password;
    private final String name;
    private final AuthRole role;
    private LocalDateTime lastLoginAt;

    private AuthUser(
            AuthUserId id,
            Email email,
            HashedPassword password,
            String name,
            AuthRole role,
            LocalDateTime lastLoginAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.password = Objects.requireNonNull(password, "password cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.role = role != null ? role : AuthRole.MEMBER;
        this.lastLoginAt = lastLoginAt;
    }

    /** Factory method for creating a new user during registration. */
    public static AuthUser register(Email email, HashedPassword password, String name, AuthRole role) {
        return new AuthUser(AuthUserId.generate(), email, password, name, role, null);
    }

    /** Factory method for reconstituting from persistence. */
    public static AuthUser reconstitute(
            AuthUserId id,
            Email email,
            HashedPassword password,
            String name,
            AuthRole role,
            LocalDateTime lastLoginAt) {
        return new AuthUser(id, email, password, name, role, lastLoginAt);
    }

    /** Records the current time as the last login time. */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /** Changes the password to a new hashed password. */
    public void changePassword(HashedPassword newPassword) {
        this.password = Objects.requireNonNull(newPassword, "new password cannot be null");
    }

    // Getters

    public AuthUserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public AuthRole getRole() {
        return role;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
