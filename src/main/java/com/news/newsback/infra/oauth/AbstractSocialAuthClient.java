package com.news.newsback.infra.oauth;

import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.global.error.BusinessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractSocialAuthClient implements SocialAuthClient {

    protected final RestTemplate restTemplate;

    protected AbstractSocialAuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> T fetchUserInfo(String url, String socialToken, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + socialToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType
            );

            T body = response.getBody();
            if (body == null) {
                throw new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID);
            }
            return body;
        } catch (HttpClientErrorException e) {
            throw new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID);
        }
    }

    @Override
    public abstract SocialUserInfo verify(SocialProvider provider, String socialToken);
}
