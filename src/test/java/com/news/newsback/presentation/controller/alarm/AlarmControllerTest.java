package com.news.newsback.presentation.controller.alarm;

import com.news.newsback.application.alarm.FcmTokenService;
import com.news.newsback.application.alarm.PushNotificationService;
import com.news.newsback.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlarmController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AlarmController 단위 테스트")
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FcmTokenService fcmTokenService;

    @MockBean
    private PushNotificationService pushNotificationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    @DisplayName("FCM 토큰을 등록 또는 갱신한다")
    void FCM_토큰_등록_갱신() throws Exception {
        mockMvc.perform(post("/api/alarms/fcm-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "token": "token-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fcmTokenService).registerToken(1L, "token-1");
    }

    @Test
    @WithMockUser
    @DisplayName("FCM 토큰을 비활성화한다")
    void FCM_토큰_비활성화() throws Exception {
        mockMvc.perform(delete("/api/alarms/fcm-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "token": "token-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fcmTokenService).disableToken(1L, "token-1");
    }

    @Test
    @WithMockUser
    @DisplayName("테스트 푸시를 발송한다")
    void 테스트_푸시_발송() throws Exception {
        mockMvc.perform(post("/api/alarms/test-push")
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
}
