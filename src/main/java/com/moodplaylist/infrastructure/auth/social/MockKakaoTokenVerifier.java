package com.moodplaylist.infrastructure.auth.social;

import com.moodplaylist.application.auth.social.SocialIdentity;
import com.moodplaylist.application.auth.social.SocialTokenVerifier;
import org.springframework.stereotype.Component;

@Component
public class MockKakaoTokenVerifier implements SocialTokenVerifier {
    @Override
    public String provider() { return "kakao"; }

    @Override
    public SocialIdentity verify(String idToken, String accessToken) {
        return new SocialIdentity("kakao-" + Math.abs(idToken.hashCode()), "kakao_" + Math.abs(idToken.hashCode()) + "@mood.local");
    }
}
