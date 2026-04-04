package com.moodplaylist.domain.recommendation.model;

public record PlaylistCandidate(
        int rank,
        String title,
        String reason,
        String emotionLink,
        String youtubeQuery,
        String youtubeUrl,
        boolean youtubeMusicSupported
) {}
