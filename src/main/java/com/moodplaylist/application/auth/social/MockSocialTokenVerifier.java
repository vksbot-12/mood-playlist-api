package com.moodplaylist.application.auth.social;

import org.springframework.stereotype.Component;

@Component
public class MockSocialTokenVerifier implements SocialTokenVerifier {
    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public SocialIdentity verify(String idToken, String accessToken) {
        String email = "mock_" + Math.abs(idToken.hashCode()) + "@mood.local";
        return new SocialIdentity("uid-" + Math.abs(idToken.hashCode()), email);
    }
}
