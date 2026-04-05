package com.moodplaylist.presentation.recommendation;

import com.moodplaylist.application.recommendation.service.RecommendationService;
import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.presentation.auth.AuthUserResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
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
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(recommendationService.recommend(userId, request.moodText()));
    }

    @GetMapping("/calendar")
    public ApiResponse<RecommendationService.CalendarMonthResult> calendar(
            @RequestParam @Min(2020) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(recommendationService.getMonth(userId, year, month));
    }

    @GetMapping("/calendar/day")
    public ApiResponse<RecommendationService.DayDetailResult> dayDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(recommendationService.getDayDetail(userId, date));
    }

    @GetMapping("/{moodLogId}/share")
    public ApiResponse<RecommendationService.ShareData> shareData(@PathVariable Long moodLogId) {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(recommendationService.getShareData(userId, moodLogId));
    }

    public record RecommendRequest(@NotBlank String moodText) {}
}
