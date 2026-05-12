package com.news.newsback.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FcmClientConfig {

    @Bean
    public FcmClient fcmClient(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging != null) {
            log.info("Firebase FCM client is configured.");
            return new FirebaseFcmClient(firebaseMessaging);
        }
        log.warn("Firebase credential is not configured. Use NoopFcmClient.");
        return new NoopFcmClient();
    }
}
