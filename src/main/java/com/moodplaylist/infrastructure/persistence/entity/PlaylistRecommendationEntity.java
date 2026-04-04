package com.moodplaylist.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_recommendations")
@Getter
@Setter
public class PlaylistRecommendationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mood_log_id", nullable = false)
    private Long moodLogId;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "emotion_link", nullable = false, columnDefinition = "TEXT")
    private String emotionLink;

    @Column(name = "youtube_query")
    private String youtubeQuery;

    @Column(name = "youtube_url", columnDefinition = "TEXT")
    private String youtubeUrl;

    @Column(name = "youtube_music_supported", nullable = false)
    private boolean youtubeMusicSupported;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
