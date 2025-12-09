package org.nkcoder.user.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.nkcoder.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for UserJpaEntity. */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") UUID excludeId);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
