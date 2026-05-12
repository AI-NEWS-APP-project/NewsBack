package com.news.newsback.domain.notification.model;

import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_histories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_histories_user_keyword_news",
                columnNames = {"user_id", "keyword_news_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_news_id", nullable = false)
    private KeywordNews keywordNews;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(nullable = false, length = 512)
    private String route;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static NotificationHistory success(User user, KeywordNews keywordNews, String title, String body, String route) {
        return NotificationHistory.builder()
                .user(user)
                .keywordNews(keywordNews)
                .title(title)
                .body(body)
                .route(route)
                .success(true)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationHistory failure(
            User user,
            KeywordNews keywordNews,
            String title,
            String body,
            String route,
            String failureReason
    ) {
        return NotificationHistory.builder()
                .user(user)
                .keywordNews(keywordNews)
                .title(title)
                .body(body)
                .route(route)
                .success(false)
                .failureReason(failureReason)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public void markAsRead() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
