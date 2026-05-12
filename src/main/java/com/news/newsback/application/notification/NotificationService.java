package com.news.newsback.application.notification;

import com.news.newsback.domain.notification.exception.NotificationErrorCode;
import com.news.newsback.domain.notification.model.NotificationHistory;
import com.news.newsback.domain.notification.repository.NotificationHistoryRepository;
import com.news.newsback.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationHistoryRepository.findByUserIdAndSuccessTrueOrderBySentAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        NotificationHistory history = notificationHistoryRepository.findByIdAndUserIdAndSuccessTrue(notificationId, userId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        history.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationHistoryRepository.markAllAsRead(userId, LocalDateTime.now());
    }
}
