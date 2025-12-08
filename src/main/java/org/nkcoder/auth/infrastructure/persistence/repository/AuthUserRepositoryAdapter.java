package org.nkcoder.auth.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.nkcoder.auth.domain.model.AuthUser;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.repository.AuthUserRepository;
import org.nkcoder.auth.infrastructure.persistence.mapper.AuthUserPersistenceMapper;
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.springframework.stereotype.Repository;

/** Adapter implementing AuthUserRepository using Spring Data JPA. */
@Repository
public class AuthUserRepositoryAdapter implements AuthUserRepository {

  private final AuthUserJpaRepository jpaRepository;
  private final AuthUserPersistenceMapper mapper;

  public AuthUserRepositoryAdapter(
      AuthUserJpaRepository jpaRepository, AuthUserPersistenceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<AuthUser> findById(AuthUserId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<AuthUser> findByEmail(Email email) {
    return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpaRepository.existsByEmail(email.value());
  }

  @Override
  public AuthUser save(AuthUser authUser) {
    boolean exists = jpaRepository.existsById(authUser.getId().value());
    var entity = exists ? mapper.toEntity(authUser) : mapper.toNewEntity(authUser);
    var savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void updateLastLoginAt(AuthUserId id, LocalDateTime lastLoginAt) {
    jpaRepository.updateLastLoginAt(id.value(), lastLoginAt);
  }
}
