package org.nkcoder.auth.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.nkcoder.auth.infrastructure.persistence.entity.AuthUserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for AuthUserJpaEntity. */
@Repository
public interface AuthUserJpaRepository extends JpaRepository<AuthUserJpaEntity, UUID> {

    Optional<AuthUserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE AuthUserJpaEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
