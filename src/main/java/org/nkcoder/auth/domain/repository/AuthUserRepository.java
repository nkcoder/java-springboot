package org.nkcoder.auth.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.nkcoder.auth.domain.model.AuthUser;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.shared.kernel.domain.valueobject.Email;

/**
 * Repository interface (port) for AuthUser persistence. Implementations are in the infrastructure
 * layer.
 */
public interface AuthUserRepository {

  Optional<AuthUser> findById(AuthUserId id);

  Optional<AuthUser> findByEmail(Email email);

  boolean existsByEmail(Email email);

  AuthUser save(AuthUser authUser);

  void updateLastLoginAt(AuthUserId id, LocalDateTime lastLoginAt);
}
