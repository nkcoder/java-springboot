package org.nkcoder.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.nkcoder.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(@Param("token") String token);

  /**
   * Prevents concurrent refresh attempts from succeeding Find token with pessimistic write lock for
   * safe token rotation.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
  Optional<RefreshToken> findByTokenForUpdate(@Param("token") String token);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
  int deleteByToken(@Param("token") String token);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.tokenFamily = :tokenFamily")
  int deleteByTokenFamily(@Param("tokenFamily") String tokenFamily);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
  int deleteByUserId(@Param("userId") UUID userId);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
