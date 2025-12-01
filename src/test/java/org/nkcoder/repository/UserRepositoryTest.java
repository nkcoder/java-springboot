package org.nkcoder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.entity.User;
import org.nkcoder.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DisplayName("UserRepository")
public class UserRepositoryTest extends BaseRepositoryTest {
  @Autowired private UserRepository userRepository;
  // JPA-aware test utility for persist/flush/clear operations
  @Autowired private TestEntityManager entityManager;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Clean slate for each test
    userRepository.deleteAll();
    entityManager.flush();
    entityManager.clear();

    // Create a fresh test user
    testUser = new User("test@example.com", "encoded-password", "Test User", Role.MEMBER, false);
    entityManager.persist(testUser);
  }

  @Nested
  @DisplayName("findByEmail")
  class FindByEmail {

    @Test
    @DisplayName("returns user when email exists")
    void returnsUsersWhenExists() {
      Optional<User> found = userRepository.findByEmail("test@example.com");

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("test@example.com");
      assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("returns empty when email does not exist")
    void returnsEmptyWhenNotExists() {
      Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("is case-sensitive")
    void isCaseSensitive() {
      // Email was stored as lowercase
      Optional<User> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

      // JPA query is case-sensitive by default
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("existsByEmail")
  class ExistsByEmail {

    @Test
    @DisplayName("returns true when email exists")
    void returnsTrueWhenExists() {
      boolean exists = userRepository.existsByEmail("test@example.com");

      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("returns false when email does not exist")
    void returnsFalseWhenNotExists() {
      boolean exists = userRepository.existsByEmail("nonexistent@example.com");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("findByEmailExcludingId")
  class FindByEmailExcludingId {

    @Test
    @DisplayName("returns empty when email belongs to same user")
    void returnsEmptyForSameUser() {
      Optional<User> found =
          userRepository.findByEmailExcludingId("test@example.com", testUser.getId());

      // Should NOT find because we're excluding this user's ID
      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("returns user when email belongs to different user")
    void returnsUserForDifferentUser() {
      // Create another user with different email
      User anotherUser =
          new User("another@example.com", "password", "Another User", Role.MEMBER, false);
      entityManager.persistAndFlush(anotherUser);

      // Search for another's email, excluding testUser's ID
      Optional<User> found =
          userRepository.findByEmailExcludingId("another@example.com", testUser.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("another@example.com");
    }

    @Test
    @DisplayName("returns empty when email does not exist")
    void returnsEmptyWhenNotExists() {
      Optional<User> found =
          userRepository.findByEmailExcludingId("nonexistent@example.com", testUser.getId());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("updateLastLoginAt")
  class UpdateLastLoginAt {

    @Test
    @DisplayName("updates last login timestamp")
    void updatesTimestamp() {

      // There is a timestamp precision issue in Java/PostgreSQL tests
      // PostgreSQL's timestamp type stores microsecond precision, but Java's LocalDateTime.now()
      // has nanosecond precision. The nanoseconds get truncated when stored, causing isEqualTo()
      // to fail.
      LocalDateTime loginTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

      int updated = userRepository.updateLastLoginAt(testUser.getId(), loginTime);

      assertThat(updated).isEqualTo(1);

      // Verify the update
      entityManager.clear(); // Clear cache to force fresh read
      User refreshed = userRepository.findById(testUser.getId()).orElseThrow();
      assertThat(refreshed.getLastLoginAt()).isEqualTo(loginTime);
    }

    @Test
    @DisplayName("returns zero for non-existent user")
    void returnsZeroForNonExistentUser() {
      int updated = userRepository.updateLastLoginAt(UUID.randomUUID(), LocalDateTime.now());

      assertThat(updated).isEqualTo(0);
    }
  }
}
