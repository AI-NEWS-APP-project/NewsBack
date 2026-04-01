package com.news.newsback.domain.user.api;

import com.news.newsback.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private Boolean globalPushEnabled;
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
