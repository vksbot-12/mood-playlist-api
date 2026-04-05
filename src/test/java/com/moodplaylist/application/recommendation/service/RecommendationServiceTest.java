package com.moodplaylist.application.recommendation.service;

import com.moodplaylist.infrastructure.persistence.entity.MoodLogEntity;
import com.moodplaylist.infrastructure.persistence.entity.SubscriptionEntity;
import com.moodplaylist.infrastructure.persistence.entity.UsageQuotaEntity;
import com.moodplaylist.infrastructure.persistence.repository.JpaMoodLogRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaPlaylistRecommendationRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaUsageQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private JpaUsageQuotaRepository usageQuotaRepository;

    @Mock
    private JpaMoodLogRepository moodLogRepository;

    @Mock
    private JpaPlaylistRecommendationRepository recommendationRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private UsageQuotaEntity quota;

    @BeforeEach
    void setUp() {
        quota = new UsageQuotaEntity();
        quota.setUserId(1L);
        quota.setFreeRemaining(2);

        when(moodLogRepository.save(any(MoodLogEntity.class))).thenAnswer(invocation -> {
            MoodLogEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return entity;
        });
    }

    @Test
    void recommend_decrementsQuota_whenUserIsNotSubscribed() {
        when(usageQuotaRepository.findByUserId(1L)).thenReturn(Optional.of(quota));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

        RecommendationService.RecommendResult result = recommendationService.recommend(1L, "기분이 좀 다운됨");

        assertEquals(1, result.freeRemaining());
        verify(usageQuotaRepository, times(1)).save(quota);
        verify(recommendationRepository, times(5)).save(any());
    }

    @Test
    void recommend_keepsQuota_whenSubscriptionIsActive() {
        SubscriptionEntity active = new SubscriptionEntity();
        active.setUserId(1L);
        active.setStatus("ACTIVE");
        active.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(usageQuotaRepository.findByUserId(1L)).thenReturn(Optional.of(quota));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(active));

        RecommendationService.RecommendResult result = recommendationService.recommend(1L, "잔잔한 밤 감성");

        assertEquals(2, result.freeRemaining());
        verify(usageQuotaRepository, never()).save(quota);
        verify(recommendationRepository, times(5)).save(any());
    }

    @Test
    void recommend_throwsWhenQuotaExhaustedAndNoSubscription() {
        quota.setFreeRemaining(0);
        when(usageQuotaRepository.findByUserId(1L)).thenReturn(Optional.of(quota));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> recommendationService.recommend(1L, "지쳤어"));

        assertEquals("quota exhausted", ex.getMessage());
        verify(moodLogRepository, never()).save(any());
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    void recommend_throwsWhenMoodTextBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.recommend(1L, "   "));

        assertEquals("mood text required", ex.getMessage());
        verify(usageQuotaRepository, never()).findByUserId(any());
        verify(moodLogRepository, never()).save(any());
    }

    @Test
    void getMonth_throwsWhenYearOutOfRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.getMonth(1L, 2019, 4));

        assertEquals("year out of range", ex.getMessage());
        verify(moodLogRepository, never()).findByUserIdAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void getMonth_throwsWhenMonthOutOfRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.getMonth(1L, 2026, 13));

        assertEquals("month out of range", ex.getMessage());
        verify(moodLogRepository, never()).findByUserIdAndCreatedAtBetween(any(), any(), any());
    }
}
