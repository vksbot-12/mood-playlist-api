package com.moodplaylist.application.subscription.service;

import com.moodplaylist.infrastructure.persistence.entity.SubscriptionEntity;
import com.moodplaylist.infrastructure.persistence.repository.JpaSubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {
    private final JpaSubscriptionRepository subscriptionRepository;

    public SubscriptionService(JpaSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public SubscriptionStatus getStatus(Long userId) {
        var sub = subscriptionRepository.findByUserId(userId).orElse(null);
        if (sub == null) return new SubscriptionStatus(false, "INACTIVE", null);
        boolean active = "ACTIVE".equalsIgnoreCase(sub.getStatus()) &&
                (sub.getExpiresAt() == null || sub.getExpiresAt().isAfter(LocalDateTime.now()));
        return new SubscriptionStatus(active, sub.getStatus(), sub.getExpiresAt());
    }

    @Transactional
    public SubscriptionStatus activateMonthly(Long userId, String provider) {
        SubscriptionEntity sub = subscriptionRepository.findByUserId(userId).orElseGet(() -> {
            SubscriptionEntity s = new SubscriptionEntity();
            s.setUserId(userId);
            return s;
        });

        sub.setStatus("ACTIVE");
        sub.setProvider(provider);
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusMonths(1));
        sub.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(sub);

        return new SubscriptionStatus(true, "ACTIVE", sub.getExpiresAt());
    }

    public record SubscriptionStatus(boolean active, String status, LocalDateTime expiresAt) {}
}
