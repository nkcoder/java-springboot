package org.nkcoder.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.*;
import org.nkcoder.enums.Role;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "users",
        indexes = {@Index(name = "idx_users_email", columnList = "email")})
@EntityListeners(AuditingEntityListener.class)
public class User {

    // Not final - JPA generates after persist
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Mutable via updateEmail()
    @Column(nullable = false, unique = true)
    private String email;

    // Mutable via changePassword()
    @Column(nullable = false)
    private String password;

    // Mutable via updateName()
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final Role role;

    // mutable via markEmailVerified()
    @Column(name = "is_email_verified", nullable = false)
    private Boolean emailVerified;

    // mutable via recordLogin()
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Set by JPA auditing
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Set by JPA auditing
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Final list, mutable contents
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<RefreshToken> refreshTokens = new ArrayList<>();

    // Constructors
    public User() {
        // Required by JPA
        this.role = Role.MEMBER;
    }

    public User(String email, String password, String name, Role role, Boolean emailVerified) {
        this.email = Objects.requireNonNull(email, "email must not be null").toLowerCase();
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.role = role != null ? role : Role.MEMBER;
        this.emailVerified = emailVerified;
    }

    // package level constructor (used by testings)
    User(UUID id, String email, String password, String name, Role role, Boolean emailVerified) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email must not be null").toLowerCase();
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.role = role != null ? role : Role.MEMBER;
        this.emailVerified = emailVerified;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public Boolean emailVerified() {
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

    // Business methods
    public void updateEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail, "email must not be null").toLowerCase();
    }

    public void updateName(String newName) {
        this.name = Objects.requireNonNull(newName, "name must not be null");
    }

    public void changePassword(String encodedPassword) {
        this.password = Objects.requireNonNull(encodedPassword, "password must not be null");
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public void recordLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // Returns an unmodifiable view of the refresh tokens
    public List<RefreshToken> getRefreshTokens() {
        return Collections.unmodifiableList(refreshTokens);
    }

    public void addRefreshToken(RefreshToken token) {
        refreshTokens.add(Objects.requireNonNull(token, "refreshToken must not be null"));
    }

    public void removeRefreshToken(RefreshToken token) {
        refreshTokens.remove(token);
    }

    @Override
    public String toString() {
        return "User{"
                + "id="
                + id
                + ", email='"
                + email
                + '\''
                + ", name='"
                + name
                + '\''
                + ", role="
                + role
                + ", emailVerified="
                + emailVerified
                + ", lastLoginAt="
                + lastLoginAt
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
