package com.news.newsback.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmClientConfig {

    @Bean
    @ConditionalOnBean(FirebaseMessaging.class)
    public FcmClient firebaseFcmClient(FirebaseMessaging firebaseMessaging) {
        return new FirebaseFcmClient(firebaseMessaging);
    }

    @Bean
    @ConditionalOnMissingBean(FcmClient.class)
    public FcmClient noopFcmClient() {
        return new NoopFcmClient();
    }
}
