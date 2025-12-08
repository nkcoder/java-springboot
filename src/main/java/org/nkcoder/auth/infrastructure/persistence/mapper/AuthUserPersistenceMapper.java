package org.nkcoder.auth.infrastructure.persistence.mapper;

import org.nkcoder.auth.domain.model.AuthUser;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.model.HashedPassword;
import org.nkcoder.auth.infrastructure.persistence.entity.AuthUserJpaEntity;
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.springframework.stereotype.Component;

/** Mapper between AuthUser domain model and AuthUserJpaEntity. */
@Component
public class AuthUserPersistenceMapper {

    public AuthUser toDomain(AuthUserJpaEntity entity) {
        return AuthUser.reconstitute(
                AuthUserId.of(entity.getId()),
                Email.of(entity.getEmail()),
                HashedPassword.of(entity.getPassword()),
                entity.getName(),
                entity.getRole(),
                entity.getLastLoginAt());
    }

    public AuthUserJpaEntity toEntity(AuthUser domain) {
        return new AuthUserJpaEntity(
                domain.getId().value(),
                domain.getEmail().value(),
                domain.getPassword().value(),
                domain.getName(),
                domain.getRole(),
                domain.getLastLoginAt());
    }

    public AuthUserJpaEntity toNewEntity(AuthUser domain) {
        AuthUserJpaEntity entity = toEntity(domain);
        entity.markAsNew();
        return entity;
    }
}
