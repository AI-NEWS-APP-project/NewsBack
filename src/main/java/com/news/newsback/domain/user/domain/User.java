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

    @Column(name = "social_provider", nullable = false, length = 20)
    private String socialProvider; // "LOCAL", "GOOGLE", "KAKAO"

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    @Column(name = "global_push_enabled", nullable = false)
    @Builder.Default
    private Boolean globalPushEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    // 비즈니스 로직: FCM 토큰 업데이트
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // 비즈니스 로직: FCM 토큰 제거 (로그아웃)
    public void clearFcmToken() {
        this.fcmToken = null;
    }

    // 비즈니스 로직: 전체 알림 설정 변경
    public void updateGlobalPushEnabled(boolean enabled) {
        this.globalPushEnabled = enabled;
    }

    // 비즈니스 로직: 알림 수신 가능 여부 확인
    public boolean canReceivePushNotification() {
        return this.globalPushEnabled && this.fcmToken != null;
    }
}
