package com.news.newsback.application.notification;

import com.news.newsback.domain.notification.model.NotificationHistory;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long keywordNewsId,
        String title,
        String body,
        String route,
        LocalDateTime sentAt,
        LocalDateTime readAt
) {

    public static NotificationResponse from(NotificationHistory history) {
        return new NotificationResponse(
                history.getId(),
                history.getKeywordNews().getId(),
                history.getTitle(),
                history.getBody(),
                history.getRoute(),
                history.getSentAt(),
                history.getReadAt()
        );
    }
}
