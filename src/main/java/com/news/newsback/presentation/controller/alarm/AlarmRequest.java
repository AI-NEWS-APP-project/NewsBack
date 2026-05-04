package com.news.newsback.presentation.controller.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AlarmRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "FCM 토큰 등록 요청")
    public static class FcmToken {

        @NotNull(message = "사용자 ID는 필수입니다")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotBlank(message = "FCM 토큰은 필수입니다")
        @Schema(description = "FCM registration token", example = "fcm-token")
        private String token;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "테스트 푸시 요청")
    public static class TestPush {

        @NotNull(message = "사용자 ID는 필수입니다")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotBlank(message = "알림 제목은 필수입니다")
        @Schema(description = "알림 제목", example = "테스트 알림")
        private String title;

        @NotBlank(message = "알림 내용은 필수입니다")
        @Schema(description = "알림 내용", example = "FCM 연동 테스트입니다.")
        private String body;
    }
}
