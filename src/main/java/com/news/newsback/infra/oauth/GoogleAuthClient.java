package com.news.newsback.infra.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.global.error.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
@Profile("!test")
public class GoogleAuthClient extends AbstractSocialAuthClient {

    @Value("${google.user-info-url:https://www.googleapis.com/oauth2/v2/userinfo}")
    private String googleUserInfoUrl;

    public GoogleAuthClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public SocialUserInfo verify(SocialProvider provider, String socialToken) {
        if (provider != SocialProvider.GOOGLE) {
            throw new IllegalArgumentException("GoogleAuthClient는 GOOGLE provider만 지원합니다.");
        }

        GoogleResponse body = fetchUserInfo(googleUserInfoUrl, socialToken, GoogleResponse.class);

        if (body.email() == null || body.email().isEmpty()) {
            throw new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID);
        }

        return new SocialUserInfo(body.email());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleResponse(String email) {
    }
}
