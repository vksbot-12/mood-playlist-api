package com.moodplaylist.presentation.recommendation;

import com.moodplaylist.application.recommendation.service.RecommendationService;
import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.presentation.auth.AuthUserResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ApiResponse<RecommendationService.RecommendResult> recommend(@Valid @RequestBody RecommendRequest request) {
        // TODO: JWT 인증 완료 후 SecurityContext에서 userId 추출
        Long userId = 1L;
        return ApiResponse.ok(recommendationService.recommend(userId, request.moodText()));
    }

    @GetMapping("/calendar")
    public ApiResponse<RecommendationService.CalendarMonthResult> calendar(
            @RequestParam @Min(2020) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        Long userId = 1L;
        return ApiResponse.ok(recommendationService.getMonth(userId, year, month));
    }

    @GetMapping("/calendar/day")
    public ApiResponse<RecommendationService.DayDetailResult> dayDetail(@RequestParam String date) {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(recommendationService.getDayDetail(userId, LocalDate.parse(date)));
    }

    public record RecommendRequest(@NotBlank String moodText) {}
}
