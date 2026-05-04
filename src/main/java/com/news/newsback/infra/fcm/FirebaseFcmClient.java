package com.news.newsback.infra.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(FirebaseMessaging.class)
public class FirebaseFcmClient implements FcmClient {

    private static final int MAX_MULTICAST_SIZE = 500;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public List<FcmSendResult> sendToTokens(List<String> tokens, PushMessage message) {
        List<FcmSendResult> results = new ArrayList<>();
        for (int start = 0; start < tokens.size(); start += MAX_MULTICAST_SIZE) {
            int end = Math.min(start + MAX_MULTICAST_SIZE, tokens.size());
            results.addAll(sendChunk(tokens.subList(start, end), message));
        }
        return results;
    }

    private List<FcmSendResult> sendChunk(List<String> tokens, PushMessage message) {
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(message.title())
                        .setBody(message.body())
                        .build())
                .putAllData(message.data())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(multicastMessage);
            List<FcmSendResult> results = new ArrayList<>();
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                String token = tokens.get(i);
                if (sendResponse.isSuccessful()) {
                    results.add(FcmSendResult.success(token));
                } else {
                    FirebaseMessagingException exception = sendResponse.getException();
                    String reason = exception == null ? "UNKNOWN" : exception.getMessagingErrorCode().name();
                    results.add(FcmSendResult.failure(token, reason, isInvalidToken(exception)));
                }
            }
            return results;
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast request failed", e);
            String reason = e.getMessagingErrorCode() == null ? "UNKNOWN" : e.getMessagingErrorCode().name();
            return tokens.stream()
                    .map(token -> FcmSendResult.failure(token, reason, isInvalidToken(e)))
                    .toList();
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException exception) {
        if (exception == null || exception.getMessagingErrorCode() == null) {
            return false;
        }
        return exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || exception.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
