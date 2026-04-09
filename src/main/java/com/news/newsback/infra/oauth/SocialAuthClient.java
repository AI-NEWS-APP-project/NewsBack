package com.news.newsback.infra.oauth;

import com.news.newsback.domain.user.domain.SocialProvider;

public interface SocialAuthClient {

    SocialUserInfo verify(SocialProvider provider, String socialToken);
}

