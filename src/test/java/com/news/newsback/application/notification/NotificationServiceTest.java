package com.news.newsback.application.notification;

import com.news.newsback.domain.notification.exception.NotificationErrorCode;
import com.news.newsback.domain.notification.model.NotificationHistory;
import com.news.newsback.domain.notification.repository.NotificationHistoryRepository;
import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @Test
    @DisplayName("인증 사용자 알림 내역을 최신순 페이지로 조회한다")
    void 알림_내역_조회() {
        NotificationHistory history = notificationHistory(1L, user(1L), keywordNews(31L));
        when(notificationHistoryRepository.findByUserIdAndSuccessTrueOrderBySentAtDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(history)));

        var result = notificationService.getNotifications(1L, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(0).title()).isEqualTo("'AI' 새 뉴스 요약");
        assertThat(result.getContent().get(0).route()).isEqualTo("/news/keyword-news/31");
    }

    @Test
    @DisplayName("본인 알림을 읽음 처리한다")
    void 알림_읽음_처리() {
        NotificationHistory history = notificationHistory(1L, user(1L), keywordNews(31L));
        when(notificationHistoryRepository.findByIdAndUserIdAndSuccessTrue(1L, 1L)).thenReturn(Optional.of(history));

        notificationService.markAsRead(1L, 1L);

        assertThat(history.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("본인 알림이 아니면 찾을 수 없음 예외를 던진다")
    void 알림_소유권_검증() {
        when(notificationHistoryRepository.findByIdAndUserIdAndSuccessTrue(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("인증 사용자의 모든 미읽음 알림을 읽음 처리한다")
    void 전체_알림_읽음_처리() {
        notificationService.markAllAsRead(1L);

        verify(notificationHistoryRepository).markAllAsRead(any(Long.class), any(LocalDateTime.class));
    }

    private NotificationHistory notificationHistory(Long id, User user, KeywordNews keywordNews) {
        NotificationHistory history = NotificationHistory.success(
                user,
                keywordNews,
                "'AI' 새 뉴스 요약",
                "관련 클러스터 3개를 바탕으로 새 요약이 생성됐습니다.",
                "/news/keyword-news/31"
        );
        ReflectionTestUtils.setField(history, "id", id);
        return history;
    }

    private User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.LOCAL)
                .globalPushEnabled(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private KeywordNews keywordNews(Long id) {
        Keyword keyword = Keyword.builder()
                .name("AI")
                .build();
        keyword.setId(9L);
        KeywordNews keywordNews = KeywordNews.builder()
                .keyword(keyword)
                .summaryText("요약")
                .clusterNewsCount(3)
                .build();
        ReflectionTestUtils.setField(keywordNews, "id", id);
        return keywordNews;
    }
}
