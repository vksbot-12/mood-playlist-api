package com.moodplaylist.infrastructure.auth.social;

import com.moodplaylist.application.auth.social.SocialIdentity;
import com.moodplaylist.application.auth.social.SocialTokenVerifier;
import org.springframework.stereotype.Component;

@Component
public class MockAppleTokenVerifier implements SocialTokenVerifier {
    @Override
    public String provider() { return "apple"; }

    @Override
    public SocialIdentity verify(String idToken, String accessToken) {
        return new SocialIdentity("apple-" + Math.abs(idToken.hashCode()), "apple_" + Math.abs(idToken.hashCode()) + "@mood.local");
    }
}
