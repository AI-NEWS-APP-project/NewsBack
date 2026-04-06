package com.news.newsback.unit.domain.user;

import com.news.newsback.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Test
    @DisplayName("User 생성 시 globalPushEnabled 기본값은 true")
    void 기본값_검증() {
        User user = User.builder()
            .email("test@example.com")
            .password("encoded")
            .socialProvider("LOCAL")
            .build();

        assertThat(user.getGlobalPushEnabled()).isTrue();
    }

    @Test
    @DisplayName("FCM 토큰 업데이트/삭제")
    void fcm_토큰_업데이트_삭제() {
        User user = User.builder()
            .email("test@example.com")
            .password("encoded")
            .socialProvider("LOCAL")
            .build();

        user.updateFcmToken("fcm-token");
        assertThat(user.getFcmToken()).isEqualTo("fcm-token");

        user.clearFcmToken();
        assertThat(user.getFcmToken()).isNull();
    }

    @Test
    @DisplayName("알림 수신 가능 여부는 globalPushEnabled && fcmToken != null")
    void canReceivePushNotification_검증() {
        User user = User.builder()
            .email("test@example.com")
            .password("encoded")
            .socialProvider("LOCAL")
            .build();

        assertThat(user.canReceivePushNotification()).isFalse();

        user.updateFcmToken("fcm-token");
        assertThat(user.canReceivePushNotification()).isTrue();

        user.updateGlobalPushEnabled(false);
        assertThat(user.canReceivePushNotification()).isFalse();
    }
}

