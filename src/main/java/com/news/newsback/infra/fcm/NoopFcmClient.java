package com.news.newsback.infra.fcm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnMissingBean(FcmClient.class)
public class NoopFcmClient implements FcmClient {

    @Override
    public List<FcmSendResult> sendToTokens(List<String> tokens, PushMessage message) {
        log.warn("FCM is not configured. Skip push notification. tokens={}, title={}", tokens.size(), message.title());
        return List.of();
    }
}
