package com.moodplaylist.infrastructure.persistence.repository;

import com.moodplaylist.infrastructure.persistence.entity.MoodLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMoodLogRepository extends JpaRepository<MoodLogEntity, Long> {
    List<MoodLogEntity> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
    List<MoodLogEntity> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime from, LocalDateTime to);
    java.util.Optional<MoodLogEntity> findByIdAndUserId(Long id, Long userId);
}
