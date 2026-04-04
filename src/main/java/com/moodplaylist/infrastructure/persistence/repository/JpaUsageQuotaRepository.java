package com.moodplaylist.infrastructure.persistence.repository;

import com.moodplaylist.infrastructure.persistence.entity.UsageQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUsageQuotaRepository extends JpaRepository<UsageQuotaEntity, Long> {
    Optional<UsageQuotaEntity> findByUserId(Long userId);
}
