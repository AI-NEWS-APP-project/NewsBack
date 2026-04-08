package com.news.newsback.domain.user.api;

import com.news.newsback.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보 응답")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @Schema(description = "전역 푸시 알림 설정 여부", example = "true")
    private Boolean globalPushEnabled;

    @Schema(description = "가입일", example = "2024-04-07T10:00:00")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .globalPushEnabled(user.getGlobalPushEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
