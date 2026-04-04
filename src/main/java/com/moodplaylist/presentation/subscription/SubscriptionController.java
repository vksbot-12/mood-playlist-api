package com.moodplaylist.presentation.subscription;

import com.moodplaylist.application.subscription.service.SubscriptionService;
import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.presentation.auth.AuthUserResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/me")
    public ApiResponse<SubscriptionService.SubscriptionStatus> getMyStatus() {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(subscriptionService.getStatus(userId));
    }

    @PostMapping("/activate-monthly")
    public ApiResponse<SubscriptionService.SubscriptionStatus> activateMonthly(@Valid @RequestBody ActivateRequest request) {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        return ApiResponse.ok(subscriptionService.activateMonthly(userId, request.provider()));
    }

    public record ActivateRequest(@NotBlank String provider) {}
}
