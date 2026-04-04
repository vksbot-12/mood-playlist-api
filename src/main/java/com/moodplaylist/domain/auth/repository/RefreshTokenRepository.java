package com.moodplaylist.domain.auth.repository;

import com.moodplaylist.infrastructure.persistence.entity.RefreshTokenEntity;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshTokenEntity save(RefreshTokenEntity entity);
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    void revokeAllByUserId(Long userId);
}
