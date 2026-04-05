package com.moodplaylist.infrastructure.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    @Test
    void constructor_throwsWhenSecretTooShort() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new JwtProvider("too-short-secret", "mood-playlist", 900, 1209600));

        assertEquals("JWT_SECRET must be at least 32 bytes for HS256", ex.getMessage());
    }

    @Test
    void issueAndParseAccessToken_withValidSecret() {
        JwtProvider provider = new JwtProvider(
                "change-me-change-me-change-me-1234567890",
                "mood-playlist",
                900,
                1209600
        );

        String token = provider.issueAccessToken(42L);
        assertNotNull(token);

        Claims claims = provider.parse(token);
        assertEquals("42", claims.getSubject());
        assertEquals("access", claims.get("typ", String.class));
        assertEquals("mood-playlist", claims.getIssuer());
    }
}
