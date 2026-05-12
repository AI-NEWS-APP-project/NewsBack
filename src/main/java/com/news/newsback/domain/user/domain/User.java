package com.news.newsback.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "global_push_enabled", nullable = false)
    @Builder.Default
    private Boolean globalPushEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    public void login(String hashedRefreshToken) {
        this.refreshToken = hashedRefreshToken;
    }

    public void updateRefreshToken(String hashedToken) {
        this.refreshToken = hashedToken;
    }

    public void logout() {
        this.refreshToken = null;
    }

    // 비즈니스 로직: 전체 알림 설정 변경
    public void updateGlobalPushEnabled(boolean enabled) {
        this.globalPushEnabled = enabled;
    }

}
