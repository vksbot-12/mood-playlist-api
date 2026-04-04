package com.moodplaylist.presentation.user;

import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.infrastructure.persistence.repository.JpaUsageQuotaRepository;
import com.moodplaylist.presentation.auth.AuthUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quotas")
public class QuotaController {
    private final JpaUsageQuotaRepository quotaRepository;

    public QuotaController(JpaUsageQuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Integer>> getMyQuota() {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        int remaining = quotaRepository.findByUserId(userId).map(q -> q.getFreeRemaining()).orElse(0);
        return ApiResponse.ok(Map.of("freeRemaining", remaining));
    }
}
