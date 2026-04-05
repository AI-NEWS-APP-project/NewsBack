package com.news.newsback.domain.user.domain;

public enum SocialProvider {
    LOCAL,
    GOOGLE,
    KAKAO;

    public static SocialProvider from(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider는 필수입니다");
        }
        return SocialProvider.valueOf(provider.trim().toUpperCase());
    }
}

