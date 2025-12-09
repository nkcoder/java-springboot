package org.nkcoder.user.infrastructure.persistence.mapper;

import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

/** Mapper between RefreshToken domain model and RefreshTokenJpaEntity. */
@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getToken(),
                TokenFamily.of(entity.getTokenFamily()),
                UserId.of(entity.getUserId()),
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
