package org.nkcoder.user.infrastructure.persistence.mapper;

import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/** Mapper between User domain model and UserJpaEntity. */
@Component
public class UserPersistenceMapper {

  public User toDomain(UserJpaEntity entity) {
    return User.reconstitute(
        UserId.of(entity.getId()),
        Email.of(entity.getEmail()),
        UserName.of(entity.getName()),
        entity.getRole(),
        entity.isEmailVerified(),
        entity.getLastLoginAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public UserJpaEntity toEntity(User user) {
    return new UserJpaEntity(
        user.getId().value(),
        user.getEmail().value(),
        user.getName().value(),
        user.getRole(),
        user.isEmailVerified(),
        user.getLastLoginAt(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public UserJpaEntity toNewEntity(User user) {
    UserJpaEntity entity = toEntity(user);
    entity.markAsNew();
    return entity;
  }
}
