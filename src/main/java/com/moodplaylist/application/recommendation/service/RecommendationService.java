package com.moodplaylist.application.recommendation.service;

import com.moodplaylist.domain.recommendation.model.PlaylistCandidate;
import com.moodplaylist.infrastructure.persistence.entity.MoodLogEntity;
import com.moodplaylist.infrastructure.persistence.entity.PlaylistRecommendationEntity;
import com.moodplaylist.infrastructure.persistence.entity.UsageQuotaEntity;
import com.moodplaylist.infrastructure.persistence.repository.JpaMoodLogRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaPlaylistRecommendationRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaUsageQuotaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final JpaUsageQuotaRepository usageQuotaRepository;
    private final JpaMoodLogRepository moodLogRepository;
    private final JpaPlaylistRecommendationRepository recommendationRepository;

    public RecommendationService(
            JpaUsageQuotaRepository usageQuotaRepository,
            JpaMoodLogRepository moodLogRepository,
            JpaPlaylistRecommendationRepository recommendationRepository
    ) {
        this.usageQuotaRepository = usageQuotaRepository;
        this.moodLogRepository = moodLogRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public RecommendResult recommend(Long userId, String moodText) {
        UsageQuotaEntity quota = usageQuotaRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("quota not found"));

        if (quota.getFreeRemaining() <= 0) {
            throw new IllegalStateException("quota exhausted");
        }

        List<PlaylistCandidate> candidates = generateMockCandidates(moodText);

        MoodLogEntity moodLog = new MoodLogEntity();
        moodLog.setUserId(userId);
        moodLog.setMoodText(moodText);
        moodLog.setMoodSummary(moodText.length() > 30 ? moodText.substring(0, 30) : moodText);
        MoodLogEntity saved = moodLogRepository.save(moodLog);

        for (PlaylistCandidate c : candidates) {
            PlaylistRecommendationEntity entity = new PlaylistRecommendationEntity();
            entity.setMoodLogId(saved.getId());
            entity.setRankNo(c.rank());
            entity.setTitle(c.title());
            entity.setReason(c.reason());
            entity.setEmotionLink(c.emotionLink());
            entity.setYoutubeQuery(c.youtubeQuery());
            entity.setYoutubeUrl(c.youtubeUrl());
            entity.setYoutubeMusicSupported(c.youtubeMusicSupported());
            recommendationRepository.save(entity);
        }

        quota.setFreeRemaining(quota.getFreeRemaining() - 1);
        usageQuotaRepository.save(quota);

        return new RecommendResult(saved.getId(), moodText, candidates, quota.getFreeRemaining());
    }

    public CalendarMonthResult getMonth(Long userId, int year, int month) {
        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1);
        var logs = moodLogRepository.findByUserIdAndCreatedAtBetween(userId, from, to);
        Map<String, Integer> byDate = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getCreatedAt().toLocalDate().toString(), Collectors.summingInt(v -> 1)));
        return new CalendarMonthResult(year, month, byDate);
    }

    public DayDetailResult getDayDetail(Long userId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        var logs = moodLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, from, to);
        if (logs.isEmpty()) {
            return new DayDetailResult(date.toString(), List.of());
        }

        List<DayItem> items = logs.stream().map(log -> {
            var recs = recommendationRepository.findByMoodLogIdOrderByRankNoAsc(log.getId());
            return new DayItem(
                    log.getId(),
                    log.getMoodText(),
                    log.getMoodSummary(),
                    log.getCreatedAt().toString(),
                    recs.stream().map(r -> new PlaylistCandidate(
                            r.getRankNo(), r.getTitle(), r.getReason(), r.getEmotionLink(),
                            r.getYoutubeQuery(), r.getYoutubeUrl(), r.isYoutubeMusicSupported()
                    )).toList()
            );
        }).toList();

        return new DayDetailResult(date.toString(), items);
    }

    private List<PlaylistCandidate> generateMockCandidates(String moodText) {
        List<PlaylistCandidate> out = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String query = moodText + " playlist " + i;
            out.add(new PlaylistCandidate(
                    i,
                    "Mood Mix " + i,
                    "입력 감정의 톤과 에너지 레벨을 반영한 추천",
                    "'" + moodText + "' 감정과 어울리는 흐름",
                    query,
                    "https://www.youtube.com/results?search_query=" + query.replace(" ", "+"),
                    true
            ));
        }
        return out;
    }

    public ShareData getShareData(Long userId, Long moodLogId) {
        var log = moodLogRepository.findByIdAndUserId(moodLogId, userId)
                .orElseThrow(() -> new IllegalArgumentException("mood log not found"));
        var recs = recommendationRepository.findByMoodLogIdOrderByRankNoAsc(moodLogId);
        String text = recs.stream()
                .map(r -> r.getRankNo() + ". " + r.getTitle() + " - " + (r.getYoutubeUrl() == null ? "" : r.getYoutubeUrl()))
                .collect(Collectors.joining("\n"));
        return new ShareData("오늘의 감정: " + log.getMoodText() + "\n" + text);
    }

    public record RecommendResult(Long moodLogId, String moodText, List<PlaylistCandidate> candidates, int freeRemaining) {}
    public record CalendarMonthResult(int year, int month, Map<String, Integer> days) {}
    public record DayDetailResult(String date, List<DayItem> items) {}
    public record DayItem(Long moodLogId, String moodText, String moodSummary, String createdAt, List<PlaylistCandidate> candidates) {}
    public record ShareData(String content) {}
}
