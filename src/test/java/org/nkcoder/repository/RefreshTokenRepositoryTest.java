package org.nkcoder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.config.DataJpaIntegrationTest;
import org.nkcoder.entity.RefreshToken;
import org.nkcoder.entity.User;
import org.nkcoder.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaIntegrationTest
@DisplayName("RefreshTokenRepository")
class RefreshTokenRepositoryTest {
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TestEntityManager entityManager;

  private User testUser;
  private final String tokenFamily = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();
    entityManager.clear();

    testUser = new User("test@example.com", "encoded-password", "Test User", Role.MEMBER, false);
    testUser = entityManager.persistAndFlush(testUser);
    entityManager.clear();
  }

  @Nested
  @DisplayName("findByToken")
  class FindByToken {

    @Test
    @DisplayName("returns token when exists")
    void returnsTokenWhenExists() {
      RefreshToken token =
          new RefreshToken(
              "valid-token", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persistAndFlush(token);
      entityManager.clear();

      Optional<RefreshToken> found = refreshTokenRepository.findByToken("valid-token");

      assertThat(found).isPresent();
      assertThat(found.get().getTokenFamily()).isEqualTo(tokenFamily);
    }

    @Test
    @DisplayName("returns empty when token does not exist")
    void returnsEmptyWhenNotExists() {
      Optional<RefreshToken> found = refreshTokenRepository.findByToken("nonexistent");

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByTokenForUpdate (pessimistic lock)")
  class FindByTokenForUpdate {

    @Test
    @DisplayName("returns token with lock when exists")
    void returnsTokenWithLock() {
      RefreshToken token =
          new RefreshToken(
              "locked-token", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persistAndFlush(token);
      entityManager.clear();

      // This executes SELECT ... FOR UPDATE
      Optional<RefreshToken> found = refreshTokenRepository.findByTokenForUpdate("locked-token");

      assertThat(found).isPresent();
      assertThat(found.get().getToken()).isEqualTo("locked-token");
    }

    @Test
    @DisplayName("returns empty when token does not exist")
    void returnsEmptyWhenNotExists() {
      Optional<RefreshToken> found = refreshTokenRepository.findByTokenForUpdate("nonexistent");

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteByTokenFamily")
  class DeleteByTokenFamily {

    @Test
    @DisplayName("deletes all tokens in family")
    void deletesAllTokensInFamily() {
      // Create multiple tokens in same family (simulating refresh rotations)
      RefreshToken token1 =
          new RefreshToken(
              "token-1", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      RefreshToken token2 =
          new RefreshToken(
              "token-2", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persist(token1);
      entityManager.persist(token2);
      entityManager.flush();
      entityManager.clear();

      int deleted = refreshTokenRepository.deleteByTokenFamily(tokenFamily);

      assertThat(deleted).isEqualTo(2);
      assertThat(refreshTokenRepository.findByToken("token-1")).isEmpty();
      assertThat(refreshTokenRepository.findByToken("token-2")).isEmpty();
    }

    @Test
    @DisplayName("does not delete tokens from other families")
    void doesNotDeleteOtherFamilies() {
      String otherFamily = UUID.randomUUID().toString();

      RefreshToken token1 =
          new RefreshToken(
              "token-1", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      RefreshToken token2 =
          new RefreshToken(
              "token-2", otherFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persist(token1);
      entityManager.persist(token2);
      entityManager.flush();
      entityManager.clear();

      refreshTokenRepository.deleteByTokenFamily(tokenFamily);

      assertThat(refreshTokenRepository.findByToken("token-1")).isEmpty();
      assertThat(refreshTokenRepository.findByToken("token-2")).isPresent(); // Still exists
    }
  }

  @Nested
  @DisplayName("deleteExpiredTokens")
  class DeleteExpiredTokens {

    @Test
    @DisplayName("deletes tokens expired before cutoff")
    void deletesExpiredTokens() {
      LocalDateTime now = LocalDateTime.now();

      // Expired token (expired yesterday)
      RefreshToken expired =
          new RefreshToken("expired-token", tokenFamily, testUser.getId(), now.minusDays(1));
      // Valid token (expires in 7 days)
      RefreshToken valid =
          new RefreshToken(
              "valid-token", UUID.randomUUID().toString(), testUser.getId(), now.plusDays(7));

      entityManager.persist(expired);
      entityManager.persist(valid);
      entityManager.flush();
      entityManager.clear();

      int deleted = refreshTokenRepository.deleteExpiredTokens(now);

      assertThat(deleted).isEqualTo(1);
      assertThat(refreshTokenRepository.findByToken("expired-token")).isEmpty();
      assertThat(refreshTokenRepository.findByToken("valid-token")).isPresent();
    }

    @Test
    @DisplayName("returns zero when no expired tokens")
    void returnsZeroWhenNoneExpired() {
      RefreshToken valid =
          new RefreshToken(
              "valid-token", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persistAndFlush(valid);

      int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

      assertThat(deleted).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("deleteByToken")
  class DeleteByToken {

    @Test
    @DisplayName("deletes single token")
    void deletesSingleToken() {
      RefreshToken token =
          new RefreshToken(
              "to-delete", tokenFamily, testUser.getId(), LocalDateTime.now().plusDays(7));
      entityManager.persistAndFlush(token);
      entityManager.clear();

      int deleted = refreshTokenRepository.deleteByToken("to-delete");

      assertThat(deleted).isEqualTo(1);
      assertThat(refreshTokenRepository.findByToken("to-delete")).isEmpty();
    }
  }
}
