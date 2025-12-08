package org.nkcoder.auth.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.nkcoder.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for RefreshTokenJpaEntity. */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenJpaEntity r WHERE r.token = :token")
    Optional<RefreshTokenJpaEntity> findByTokenForUpdate(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.tokenFamily = :tokenFamily")
    void deleteByTokenFamily(@Param("tokenFamily") String tokenFamily);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
