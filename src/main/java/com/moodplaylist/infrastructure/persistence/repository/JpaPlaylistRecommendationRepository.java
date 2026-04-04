package com.moodplaylist.infrastructure.persistence.repository;

import com.moodplaylist.infrastructure.persistence.entity.PlaylistRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPlaylistRecommendationRepository extends JpaRepository<PlaylistRecommendationEntity, Long> {
    List<PlaylistRecommendationEntity> findByMoodLogIdOrderByRankNoAsc(Long moodLogId);
}
