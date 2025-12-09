package org.nkcoder.user.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.repository.RefreshTokenRepository;
import org.nkcoder.user.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import org.springframework.stereotype.Repository;

/** Adapter implementing RefreshTokenRepository using Spring Data JPA. */
@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenPersistenceMapper mapper;

    public RefreshTokenRepositoryAdapter(
            RefreshTokenJpaRepository jpaRepository, RefreshTokenPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenExclusively(String token) {
        return jpaRepository.findByTokenForUpdate(token).map(mapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        boolean exists = jpaRepository.existsById(refreshToken.getId());
        var entity = exists ? mapper.toEntity(refreshToken) : mapper.toNewEntity(refreshToken);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    public void deleteByTokenFamily(TokenFamily tokenFamily) {
        jpaRepository.deleteByTokenFamily(tokenFamily.value());
    }

    @Override
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(userId.value());
    }

    @Override
    public void deleteExpiredTokens(LocalDateTime now) {
        jpaRepository.deleteExpiredTokens(now);
    }
}
