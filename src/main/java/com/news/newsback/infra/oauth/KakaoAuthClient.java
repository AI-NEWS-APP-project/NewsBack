package com.news.newsback.infra.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.global.error.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("!test")
public class KakaoAuthClient extends AbstractSocialAuthClient {

    @Value("${kakao.user-info-url:https://kapi.kakao.com/v2/user/me}")
    private String kakaoUserInfoUrl;

    public KakaoAuthClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public SocialUserInfo verify(SocialProvider provider, String socialToken) {
        if (provider != SocialProvider.KAKAO) throw new IllegalArgumentException("KakaoAuthClient는 KAKAO provider만 지원합니다.");

        KakaoResponse body = fetchUserInfo(kakaoUserInfoUrl, socialToken, KakaoResponse.class);

        if (body.kakaoAccount() == null || body.kakaoAccount().email() == null || body.kakaoAccount().email().isEmpty()) {
            throw new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID);
        }

        return new SocialUserInfo(body.kakaoAccount().email());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoResponse(
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record KakaoAccount(String email) {}
    }
}
