package com.news.newsback.infra.oauth;

import com.news.newsback.domain.user.domain.SocialProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SocialAuthClientFactory {

    private final KakaoAuthClient kakaoAuthClient;
    private final GoogleAuthClient googleAuthClient;

    public SocialAuthClientFactory(
        KakaoAuthClient kakaoAuthClient,
        GoogleAuthClient googleAuthClient
    ) {
        this.kakaoAuthClient = kakaoAuthClient;
        this.googleAuthClient = googleAuthClient;
    }

    public SocialAuthClient getClient(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoAuthClient;
            case GOOGLE -> googleAuthClient;
            case LOCAL -> throw new IllegalArgumentException("LOCAL provider는 소셜 로그인을 지원하지 않습니다.");
        };
    }
}
