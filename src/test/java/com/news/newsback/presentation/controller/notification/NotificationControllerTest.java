package com.news.newsback.presentation.controller.notification;

import com.news.newsback.application.notification.FcmTokenService;
import com.news.newsback.application.notification.NotificationResponse;
import com.news.newsback.application.notification.NotificationService;
import com.news.newsback.application.notification.PushNotificationService;
import com.news.newsback.config.TestSecurityConfig;
import com.news.newsback.global.security.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
@DisplayName("NotificationController 단위 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FcmTokenService fcmTokenService;

    @MockBean
    private PushNotificationService pushNotificationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("FCM 토큰을 등록 또는 갱신한다")
    void FCM_토큰_등록_갱신() throws Exception {
        mockMvc.perform(post("/notifications/fcm-token")
                        .with(authentication(authenticatedUser(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "token-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fcmTokenService).registerToken(1L, "token-1");
    }

    @Test
    @DisplayName("FCM 토큰을 비활성화한다")
    void FCM_토큰_비활성화() throws Exception {
        mockMvc.perform(delete("/notifications/fcm-token")
                        .with(authentication(authenticatedUser(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "token-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fcmTokenService).disableToken(1L, "token-1");
    }

    @Test
    @DisplayName("테스트 푸시를 발송한다")
    void 테스트_푸시_발송() throws Exception {
        mockMvc.perform(post("/notifications/test-push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "테스트 알림",
                                  "body": "FCM 연동 테스트입니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(pushNotificationService).sendToUser(1L, "테스트 알림", "FCM 연동 테스트입니다.");
    }

    @Test
    @DisplayName("인증 사용자 알림 내역을 조회한다")
    void 알림_내역_조회() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                31L,
                "'AI' 새 뉴스 요약",
                "관련 클러스터 3개를 바탕으로 새 요약이 생성됐습니다.",
                "/news/keyword-news/31",
                LocalDateTime.of(2026, 5, 8, 16, 0),
                null
        );
        when(notificationService.getNotifications(1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/notifications")
                        .with(authentication(authenticatedUser(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("'AI' 새 뉴스 요약"))
                .andExpect(jsonPath("$.data.content[0].route").value("/news/keyword-news/31"));

        verify(notificationService).getNotifications(1L, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("인증 사용자 알림을 읽음 처리한다")
    void 알림_읽음_처리() throws Exception {
        mockMvc.perform(patch("/notifications/1/read")
                        .with(authentication(authenticatedUser(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAsRead(1L, 1L);
    }

    @Test
    @DisplayName("인증 사용자 전체 알림을 읽음 처리한다")
    void 전체_알림_읽음_처리() throws Exception {
        mockMvc.perform(patch("/notifications/read-all")
                        .with(authentication(authenticatedUser(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead(1L);
    }

    private UsernamePasswordAuthenticationToken authenticatedUser(Long userId) {
        return new UsernamePasswordAuthenticationToken(new AuthenticatedUser(userId), null, List.of());
    }
}
