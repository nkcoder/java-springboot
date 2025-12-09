package org.nkcoder.user.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.repository.UserRepository;
import org.nkcoder.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.springframework.stereotype.Repository;

/** Adapter implementing UserRepository port using JPA. */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        boolean exists = jpaRepository.existsById(user.getId().value());
        var entity = exists ? mapper.toEntity(user) : mapper.toNewEntity(user);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByEmailExcludingId(Email email, UserId excludeId) {
        return jpaRepository.existsByEmailExcludingId(email.value(), excludeId.value());
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void updateLastLoginAt(UserId id, LocalDateTime lastLoginAt) {
        jpaRepository.updateLastLoginAt(id.value(), lastLoginAt);
    }
}
