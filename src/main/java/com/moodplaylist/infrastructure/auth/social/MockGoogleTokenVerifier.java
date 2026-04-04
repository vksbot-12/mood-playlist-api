package com.moodplaylist.infrastructure.auth.social;

import com.moodplaylist.application.auth.social.SocialIdentity;
import com.moodplaylist.application.auth.social.SocialTokenVerifier;
import org.springframework.stereotype.Component;

@Component
public class MockGoogleTokenVerifier implements SocialTokenVerifier {
    @Override
    public String provider() { return "google"; }

    @Override
    public SocialIdentity verify(String idToken, String accessToken) {
        return new SocialIdentity("google-" + Math.abs(idToken.hashCode()), "google_" + Math.abs(idToken.hashCode()) + "@mood.local");
    }
}
