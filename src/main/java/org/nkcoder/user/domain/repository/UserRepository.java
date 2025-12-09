package org.nkcoder.user.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;

/** Repository interface (port) for User aggregate. */
public interface UserRepository {

    /** Saves a user (create or update). */
    User save(User user);

    /** Finds a user by their ID. */
    Optional<User> findById(UserId id);

    /** Finds a user by their email address. */
    Optional<User> findByEmail(Email email);

    /** Checks if an email exists. */
    boolean existsByEmail(Email email);

    /** Checks if an email is already in use by another user. */
    boolean existsByEmailExcludingId(Email email, UserId excludeId);

    /** Finds all users (for admin operations). */
    List<User> findAll();

    /** Deletes a user by ID. */
    void deleteById(UserId id);

    /** Checks if a user exists by ID. */
    boolean existsById(UserId id);

    /** Updates the last login timestamp for a user. */
    void updateLastLoginAt(UserId id, LocalDateTime lastLoginAt);
}
