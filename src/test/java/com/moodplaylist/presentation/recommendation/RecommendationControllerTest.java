package com.moodplaylist.presentation.recommendation;

import com.moodplaylist.application.recommendation.service.RecommendationService;
import com.moodplaylist.domain.recommendation.model.PlaylistCandidate;
import com.moodplaylist.infrastructure.auth.model.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recommend_usesAuthenticatedUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthUser(7L), null, List.of())
        );

        RecommendationService.RecommendResult result = new RecommendationService.RecommendResult(
                10L,
                "오늘은 밝음",
                List.of(new PlaylistCandidate(1, "title", "reason", "emotion", "query", "url", true)),
                9
        );
        when(recommendationService.recommend(7L, "오늘은 밝음")).thenReturn(result);

        RecommendationController controller = new RecommendationController(recommendationService);
        var response = controller.recommend(new RecommendationController.RecommendRequest("오늘은 밝음"));

        assertEquals(true, response.success());
        assertEquals(10L, response.data().moodLogId());
        verify(recommendationService).recommend(7L, "오늘은 밝음");
    }

    @Test
    void calendar_throwsUnauthorizedWithoutAuthentication() {
        RecommendationController controller = new RecommendationController(recommendationService);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> controller.calendar(2026, 4));

        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    void calendar_usesAuthenticatedUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthUser(7L), null, List.of())
        );

        RecommendationService.CalendarMonthResult monthResult =
                new RecommendationService.CalendarMonthResult(2026, 4, Map.of("2026-04-05", 2));
        when(recommendationService.getMonth(7L, 2026, 4)).thenReturn(monthResult);

        RecommendationController controller = new RecommendationController(recommendationService);
        var response = controller.calendar(2026, 4);

        assertEquals(true, response.success());
        assertEquals(1, response.data().days().size());
        verify(recommendationService).getMonth(7L, 2026, 4);
    }

    @Test
    void dayDetail_usesAuthenticatedUserIdAndLocalDate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthUser(7L), null, List.of())
        );

        LocalDate date = LocalDate.of(2026, 4, 5);
        RecommendationService.DayDetailResult dayResult =
                new RecommendationService.DayDetailResult("2026-04-05", List.of());
        when(recommendationService.getDayDetail(7L, date)).thenReturn(dayResult);

        RecommendationController controller = new RecommendationController(recommendationService);
        var response = controller.dayDetail(date);

        assertEquals(true, response.success());
        assertEquals("2026-04-05", response.data().date());
        verify(recommendationService).getDayDetail(7L, date);
    }
}
