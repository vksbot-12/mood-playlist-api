package com.moodplaylist.application.auth.service;

import com.moodplaylist.domain.auth.model.TokenPair;
import com.moodplaylist.infrastructure.auth.JwtProvider;
import com.moodplaylist.infrastructure.persistence.entity.RefreshTokenEntity;
import com.moodplaylist.infrastructure.persistence.entity.UsageQuotaEntity;
import com.moodplaylist.infrastructure.persistence.entity.UserEntity;
import com.moodplaylist.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaUsageQuotaRepository;
import com.moodplaylist.infrastructure.persistence.repository.JpaUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Service
public class AuthService {
    private final JwtProvider jwtProvider;
    private final JpaUserRepository userRepository;
    private final JpaUsageQuotaRepository usageQuotaRepository;
    private final JpaRefreshTokenRepository refreshTokenRepository;

    public AuthService(
            JwtProvider jwtProvider,
            JpaUserRepository userRepository,
            JpaUsageQuotaRepository usageQuotaRepository,
            JpaRefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.usageQuotaRepository = usageQuotaRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenPair socialLogin(String provider, String idToken) {
        // MVP: provider 검증 어댑터 연동 전까지 deterministic mock email
        String email = provider + "_" + Math.abs(idToken.hashCode()) + "@mood.local";
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity created = new UserEntity();
            created.setEmail(email);
            created.setNickname(provider + " user");
            UserEntity saved = userRepository.save(created);

            UsageQuotaEntity quota = new UsageQuotaEntity();
            quota.setUserId(saved.getId());
            quota.setFreeRemaining(10);
            usageQuotaRepository.save(quota);
            return saved;
        });

        return issueAndPersistTokenPair(user.getId());
    }

    @Transactional
    public TokenPair refresh(String refreshToken) {
        Claims claims = jwtProvider.parse(refreshToken);
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new IllegalArgumentException("invalid token type");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String oldHash = hash(refreshToken);

        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found"));

        if (stored.isRevoked()) {
            stored.setReuseDetected(true);
            refreshTokenRepository.save(stored);
            refreshTokenRepository.findByUserIdAndRevokedFalse(userId).forEach(t -> {
                t.setRevoked(true);
                t.setRevokedAt(LocalDateTime.now());
            });
            throw new IllegalStateException("refresh token reuse detected");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("refresh token expired");
        }

        TokenPair newPair = issueAndPersistTokenPair(userId);
        stored.setRevoked(true);
        stored.setRevokedAt(LocalDateTime.now());
        stored.setReplacedByTokenHash(hash(newPair.refreshToken()));
        refreshTokenRepository.save(stored);

        return newPair;
    }

    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = hash(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private TokenPair issueAndPersistTokenPair(Long userId) {
        String access = jwtProvider.issueAccessToken(userId);
        String refresh = jwtProvider.issueRefreshToken(userId);

        Claims claims = jwtProvider.parse(refresh);
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUserId(userId);
        token.setTokenHash(hash(refresh));
        token.setExpiresAt(LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC));
        refreshTokenRepository.save(token);

        return new TokenPair(access, refresh);
    }

    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }
}
