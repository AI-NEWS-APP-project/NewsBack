package com.news.newsback.application.alarm;

import com.news.newsback.domain.alarm.model.FcmToken;
import com.news.newsback.domain.alarm.model.NotificationHistory;
import com.news.newsback.domain.alarm.repository.FcmTokenRepository;
import com.news.newsback.domain.alarm.repository.NotificationHistoryRepository;
import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.UserKeyword;
import com.news.newsback.domain.keyword.domain.UserKeywordRepository;
import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.news.model.TodayNewsSummary;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.infra.fcm.FcmClient;
import com.news.newsback.infra.fcm.FcmSendResult;
import com.news.newsback.infra.fcm.PushMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService 단위 테스트")
class PushNotificationServiceTest {

    @InjectMocks
    private PushNotificationService pushNotificationService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @Mock
    private UserKeywordRepository userKeywordRepository;

    @Mock
    private FcmClient fcmClient;

    @Test
    @DisplayName("키워드 뉴스 요약 알림을 구독자 활성 토큰으로 발송하고 이력을 저장한다")
    void 키워드_뉴스_알림_발송_성공() {
        KeywordNews keywordNews = keywordNews(31L, keyword(9L, "AI"));
        User user = user(1L, true);
        FcmToken token = FcmToken.create(user, "token-1");

        when(userKeywordRepository.findByKeywordId(9L)).thenReturn(List.of(new UserKeyword(1L, keywordNews.getKeyword())));
        when(notificationHistoryRepository.existsByUserIdAndKeywordNewsId(1L, 31L)).thenReturn(false);
        when(fcmTokenRepository.findEnabledTokensByUserIdsWithPushEnabled(List.of(1L))).thenReturn(List.of(token));
        when(fcmClient.sendToTokens(eq(List.of("token-1")), any(PushMessage.class)))
                .thenReturn(List.of(FcmSendResult.success("token-1")));

        pushNotificationService.sendKeywordNews(keywordNews);

        ArgumentCaptor<PushMessage> messageCaptor = ArgumentCaptor.forClass(PushMessage.class);
        verify(fcmClient).sendToTokens(eq(List.of("token-1")), messageCaptor.capture());
        assertThat(messageCaptor.getValue().title()).isEqualTo("'AI' 새 뉴스 요약");
        assertThat(messageCaptor.getValue().body()).isEqualTo("관련 클러스터 3개를 바탕으로 새 요약이 생성됐습니다.");
        assertThat(messageCaptor.getValue().data())
                .containsEntry("type", "KEYWORD_NEWS")
                .containsEntry("keywordId", "9")
                .containsEntry("keywordNewsId", "31")
                .containsEntry("route", "/news/keyword-news/31");

        verify(notificationHistoryRepository).save(argThat(history ->
                history.getUser().equals(user)
                        && history.getKeywordNews().equals(keywordNews)
                        && history.isSuccess()
                        && history.getFailureReason() == null
        ));
    }

    @Test
    @DisplayName("이미 발송한 키워드 뉴스 알림은 다시 발송하지 않는다")
    void 키워드_뉴스_알림_중복_스킵() {
        KeywordNews keywordNews = keywordNews(31L, keyword(9L, "AI"));
        when(userKeywordRepository.findByKeywordId(9L)).thenReturn(List.of(new UserKeyword(1L, keywordNews.getKeyword())));
        when(notificationHistoryRepository.existsByUserIdAndKeywordNewsId(1L, 31L)).thenReturn(true);

        pushNotificationService.sendKeywordNews(keywordNews);

        verifyNoInteractions(fcmTokenRepository, fcmClient);
        verify(notificationHistoryRepository, never()).save(any(NotificationHistory.class));
    }

    @Test
    @DisplayName("키워드 뉴스 알림 실패 토큰은 비활성화하고 실패 이력을 저장한다")
    void 키워드_뉴스_알림_실패_토큰_처리() {
        KeywordNews keywordNews = keywordNews(31L, keyword(9L, "AI"));
        User user = user(1L, true);
        FcmToken token = FcmToken.create(user, "token-1");

        when(userKeywordRepository.findByKeywordId(9L)).thenReturn(List.of(new UserKeyword(1L, keywordNews.getKeyword())));
        when(notificationHistoryRepository.existsByUserIdAndKeywordNewsId(1L, 31L)).thenReturn(false);
        when(fcmTokenRepository.findEnabledTokensByUserIdsWithPushEnabled(List.of(1L))).thenReturn(List.of(token));
        when(fcmClient.sendToTokens(eq(List.of("token-1")), any(PushMessage.class)))
                .thenReturn(List.of(FcmSendResult.failure("token-1", "UNREGISTERED", true)));

        pushNotificationService.sendKeywordNews(keywordNews);

        assertThat(token.isEnabled()).isFalse();
        verify(notificationHistoryRepository).save(argThat(history ->
                history.getUser().equals(user)
                        && history.getKeywordNews().equals(keywordNews)
                        && !history.isSuccess()
                        && history.getFailureReason().equals("UNREGISTERED")
        ));
    }

    @Test
    @DisplayName("일일 뉴스 요약 알림은 global push enabled 사용자 활성 토큰에만 발송한다")
    void 일일_뉴스_알림_발송() {
        TodayNewsSummary summary = TodayNewsSummary.builder()
                .title("최근 주요 뉴스 종합")
                .summary("요약")
                .newsCount(20)
                .build();
        ReflectionTestUtils.setField(summary, "id", 7L);

        when(fcmTokenRepository.findAllEnabledTokensWithPushEnabled())
                .thenReturn(List.of(FcmToken.create(user(1L, true), "token-1")));
        when(fcmClient.sendToTokens(eq(List.of("token-1")), any(PushMessage.class)))
                .thenReturn(List.of(FcmSendResult.success("token-1")));

        pushNotificationService.sendTodayNewsSummary(summary);

        ArgumentCaptor<PushMessage> messageCaptor = ArgumentCaptor.forClass(PushMessage.class);
        verify(fcmClient).sendToTokens(eq(List.of("token-1")), messageCaptor.capture());
        assertThat(messageCaptor.getValue().title()).isEqualTo("오늘의 주요 뉴스");
        assertThat(messageCaptor.getValue().body()).isEqualTo("AI가 정리한 오늘의 핵심 뉴스 20개를 확인해보세요.");
        assertThat(messageCaptor.getValue().data())
                .containsEntry("type", "TODAY_NEWS")
                .containsEntry("summaryId", "7")
                .containsEntry("route", "/news/daily-briefings/7");
        verifyNoInteractions(notificationHistoryRepository);
    }

    private User user(Long id, boolean globalPushEnabled) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.LOCAL)
                .globalPushEnabled(globalPushEnabled)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Keyword keyword(Long id, String name) {
        Keyword keyword = Keyword.builder()
                .name(name)
                .build();
        keyword.setId(id);
        return keyword;
    }

    private KeywordNews keywordNews(Long id, Keyword keyword) {
        KeywordNews keywordNews = KeywordNews.builder()
                .keyword(keyword)
                .summaryText("요약")
                .clusterNewsCount(3)
                .build();
        ReflectionTestUtils.setField(keywordNews, "id", id);
        return keywordNews;
    }
}
