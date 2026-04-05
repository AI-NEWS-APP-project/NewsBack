package com.news.newsback.infra.ai;

import com.news.newsback.domain.user.domain.SocialProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockSocialAuthClient implements SocialAuthClient {

    @Override
    public SocialUserInfo verify(SocialProvider provider, String socialToken) {
        if (socialToken == null || !socialToken.startsWith("mock-valid:")) {
            throw new IllegalArgumentException("유효하지 않은 소셜 토큰입니다.");
        }
        String email = socialToken.substring("mock-valid:".length()).trim().toLowerCase();
        if (email.isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 소셜 토큰입니다.");
        }
        return new SocialUserInfo(email);
    }
}

