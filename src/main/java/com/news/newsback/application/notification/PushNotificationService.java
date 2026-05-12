package com.news.newsback.application.notification;

import com.news.newsback.domain.notification.model.FcmToken;
import com.news.newsback.domain.notification.model.NotificationHistory;
import com.news.newsback.domain.notification.repository.FcmTokenRepository;
import com.news.newsback.domain.notification.repository.NotificationHistoryRepository;
import com.news.newsback.domain.keyword.domain.UserKeywordRepository;
import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.news.model.TodayNewsSummary;
import com.news.newsback.domain.news.repository.KeywordNewsRepository;
import com.news.newsback.infra.fcm.FcmClient;
import com.news.newsback.infra.fcm.FcmSendResult;
import com.news.newsback.infra.fcm.PushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushNotificationService {

    private static final long TEST_PUSH_KEYWORD_NEWS_ID = 1L;

    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final KeywordNewsRepository keywordNewsRepository;
    private final FcmClient fcmClient;

    @Transactional
    public void sendKeywordNews(KeywordNews keywordNews) {
        List<Long> userIds = userKeywordRepository.findByKeywordId(keywordNews.getKeyword().getId())
                .stream()
                .map(userKeyword -> userKeyword.getUserId())
                .filter(userId -> !notificationHistoryRepository.existsByUserIdAndKeywordNewsId(userId, keywordNews.getId()))
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findEnabledTokensByUserIdsWithPushEnabled(userIds);
        sendAndSaveKeywordHistories(tokens, buildKeywordNewsMessage(keywordNews), keywordNews);
    }

    @Transactional
    public void sendTodayNewsSummary(TodayNewsSummary summary) {
        List<FcmToken> tokens = fcmTokenRepository.findAllEnabledTokensWithPushEnabled();
        send(tokens, buildTodayNewsMessage(summary));
    }

    @Transactional
    public void sendToUser(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findEnabledTokensByUserIdsWithPushEnabled(List.of(userId));
        PushMessage message = new PushMessage(
                title,
                body,
                Map.of(
                        "type", "TEST",
                        "route", "/"
                )
        );
        List<FcmSendResult> results = send(tokens, message);
        saveTestPushHistories(tokens, results, message);
    }

    private void sendAndSaveKeywordHistories(List<FcmToken> tokens, PushMessage message, KeywordNews keywordNews) {
        List<FcmSendResult> results = send(tokens, message);
        saveHistories(tokens, results, message, keywordNews);
    }

    private List<FcmSendResult> send(List<FcmToken> tokens, PushMessage message) {
        if (tokens.isEmpty()) {
            log.info("Skip push notification because no enabled FCM tokens found. title={}", message.title());
            return List.of();
        }

        List<String> tokenValues = tokens.stream()
                .map(FcmToken::getToken)
                .toList();
        List<FcmSendResult> results = fcmClient.sendToTokens(tokenValues, message);
        long successCount = results.stream().filter(FcmSendResult::success).count();
        long failureCount = results.size() - successCount;
        log.info(
                "Push notification requested. tokens={}, success={}, failure={}, title={}",
                tokenValues.size(),
                successCount,
                failureCount,
                message.title()
        );
        disableInvalidTokens(tokens, results);
        return results;
    }

    private void saveTestPushHistories(List<FcmToken> tokens, List<FcmSendResult> results, PushMessage message) {
        if (tokens.isEmpty() || results.isEmpty()) {
            return;
        }

        keywordNewsRepository.findById(TEST_PUSH_KEYWORD_NEWS_ID).ifPresentOrElse(
                keywordNews -> saveHistories(tokens, results, message, keywordNews),
                () -> log.warn("Skip test push notification history because keywordNewsId={} does not exist.", TEST_PUSH_KEYWORD_NEWS_ID)
        );
    }

    private void saveHistories(List<FcmToken> tokens, List<FcmSendResult> results, PushMessage message, KeywordNews keywordNews) {
        Map<String, FcmSendResult> resultByToken = results.stream()
                .collect(Collectors.toMap(FcmSendResult::token, Function.identity()));

        Map<Long, List<FcmToken>> tokensByUser = tokens.stream()
                .filter(token -> resultByToken.containsKey(token.getToken()))
                .collect(Collectors.groupingBy(token -> token.getUser().getId()));

        for (List<FcmToken> userTokens : tokensByUser.values()) {
            FcmToken representativeToken = userTokens.get(0);
            Long userId = representativeToken.getUser().getId();
            if (notificationHistoryRepository.existsByUserIdAndKeywordNewsId(userId, keywordNews.getId())) {
                continue;
            }

            List<FcmSendResult> userResults = userTokens.stream()
                    .map(token -> resultByToken.get(token.getToken()))
                    .toList();

            if (userResults.stream().anyMatch(FcmSendResult::success)) {
                notificationHistoryRepository.save(NotificationHistory.success(
                        representativeToken.getUser(),
                        keywordNews,
                        message.title(),
                        message.body(),
                        message.data().get("route")
                ));
            } else {
                notificationHistoryRepository.save(NotificationHistory.failure(
                        representativeToken.getUser(),
                        keywordNews,
                        message.title(),
                        message.body(),
                        message.data().get("route"),
                        summarizeFailureReasons(userResults)
                ));
            }
        }
    }

    private String summarizeFailureReasons(List<FcmSendResult> results) {
        return results.stream()
                .map(FcmSendResult::failureReason)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private void disableInvalidTokens(List<FcmToken> tokens, List<FcmSendResult> results) {
        Map<String, FcmToken> tokenMap = tokens.stream()
                .collect(Collectors.toMap(FcmToken::getToken, Function.identity()));

        results.stream()
                .filter(result -> !result.success() && result.invalidToken())
                .map(result -> tokenMap.get(result.token()))
                .filter(token -> token != null)
                .forEach(FcmToken::disable);
    }

    private PushMessage buildTodayNewsMessage(TodayNewsSummary summary) {
        return new PushMessage(
                "오늘의 주요 뉴스",
                "AI가 정리한 오늘의 핵심 뉴스 " + summary.getNewsCount() + "개를 확인해보세요.",
                Map.of(
                        "type", "TODAY_NEWS",
                        "summaryId", String.valueOf(summary.getId()),
                        "route", "/news/daily-briefings/" + summary.getId()
                )
        );
    }

    private PushMessage buildKeywordNewsMessage(KeywordNews keywordNews) {
        String keywordName = keywordNews.getKeyword().getName();
        return new PushMessage(
                "'" + keywordName + "' 새 뉴스 요약",
                "관련 클러스터 " + keywordNews.getClusterNewsCount() + "개를 바탕으로 새 요약이 생성됐습니다.",
                Map.of(
                        "type", "KEYWORD_NEWS",
                        "keywordId", String.valueOf(keywordNews.getKeyword().getId()),
                        "keywordNewsId", String.valueOf(keywordNews.getId()),
                        "route", "/news/keyword-news/" + keywordNews.getId()
                )
        );
    }
}
