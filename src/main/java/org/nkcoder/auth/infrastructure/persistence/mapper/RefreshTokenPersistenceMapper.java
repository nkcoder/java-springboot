package org.nkcoder.auth.infrastructure.persistence.mapper;

import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.model.RefreshToken;
import org.nkcoder.auth.domain.model.TokenFamily;
import org.nkcoder.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

/** Mapper between RefreshToken domain model and RefreshTokenJpaEntity. */
@Component
public class RefreshTokenPersistenceMapper {

  public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
    return RefreshToken.reconstitute(
        entity.getId(),
        entity.getToken(),
        TokenFamily.of(entity.getTokenFamily()),
        AuthUserId.of(entity.getUserId()),
        entity.getExpiresAt(),
        entity.getCreatedAt());
  }

  public RefreshTokenJpaEntity toEntity(RefreshToken domain) {
    return new RefreshTokenJpaEntity(
        domain.getId(),
        domain.getToken(),
        domain.getTokenFamily().value(),
        domain.getUserId().value(),
        domain.getExpiresAt(),
        domain.getCreatedAt());
  }

  public RefreshTokenJpaEntity toNewEntity(RefreshToken domain) {
    RefreshTokenJpaEntity entity = toEntity(domain);
    entity.markAsNew();
    return entity;
  }
}
