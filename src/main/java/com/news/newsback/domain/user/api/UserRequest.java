package com.news.newsback.domain.user.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원가입 요청")
    public static class Signup {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Schema(description = "사용자 이메일", example = "test@example.com")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Schema(description = "사용자 비밀번호 (최소 8자)", example = "password123")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 요청")
    public static class Login {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Schema(description = "사용자 이메일", example = "test@example.com")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "사용자 비밀번호", example = "password123")
        private String password;

        @Schema(description = "FCM 기기 토큰 (푸시 알림용)", example = "fcm-token-abc-123")
        private String fcmToken;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 로그인 요청")
    public static class SocialLogin {
        @NotBlank(message = "소셜 토큰은 필수입니다")
        @Schema(description = "소셜 서비스로부터 발급받은 액세스 토큰", example = "google_access_token_xyz")
        private String socialToken;

        @Schema(description = "FCM 기기 토큰 (푸시 알림용)", example = "fcm-token-abc-123")
        private String fcmToken;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토큰 재발급 요청")
    public static class Refresh {
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        @Schema(description = "재발급에 사용할 리프레시 토큰", example = "jwt_refresh_token_here")
        private String refreshToken;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그아웃 요청")
    public static class Logout {
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        @Schema(description = "로그아웃할 사용자의 리프레시 토큰", example = "jwt_refresh_token_here")
        private String refreshToken;
    }
}
