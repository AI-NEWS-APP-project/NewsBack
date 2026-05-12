package com.news.newsback.domain.notification.model;

import com.news.newsback.domain.user.domain.User;
import com.news.newsback.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public static FcmToken create(User user, String token) {
        return FcmToken.builder()
                .user(user)
                .token(token)
                .enabled(true)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }

    public void reactivate(User user) {
        this.user = user;
        this.enabled = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
    }
}
