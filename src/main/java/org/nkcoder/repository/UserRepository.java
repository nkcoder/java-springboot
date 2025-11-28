package org.nkcoder.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.nkcoder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.id != :excludeId")
    Optional<User> findByEmailExcludingId(@Param("email") String email, @Param("excludeId") UUID excludeId);
}
