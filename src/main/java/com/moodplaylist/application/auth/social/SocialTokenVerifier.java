package com.moodplaylist.application.auth.social;

public interface SocialTokenVerifier {
    String provider();
    SocialIdentity verify(String idToken, String accessToken);
}
