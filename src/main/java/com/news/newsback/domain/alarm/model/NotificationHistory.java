package com.news.newsback.domain.alarm.model;

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

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public static NotificationHistory success(User user, KeywordNews keywordNews) {
        return NotificationHistory.builder()
                .user(user)
                .keywordNews(keywordNews)
                .success(true)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationHistory failure(User user, KeywordNews keywordNews, String failureReason) {
        return NotificationHistory.builder()
                .user(user)
                .keywordNews(keywordNews)
                .success(false)
                .failureReason(failureReason)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
