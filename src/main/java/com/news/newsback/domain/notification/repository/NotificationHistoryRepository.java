package com.news.newsback.domain.notification.repository;

import com.news.newsback.domain.notification.model.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    boolean existsByUserIdAndKeywordNewsId(Long userId, Long keywordNewsId);

    Page<NotificationHistory> findByUserIdAndSuccessTrueOrderBySentAtDesc(Long userId, Pageable pageable);

    Optional<NotificationHistory> findByIdAndUserIdAndSuccessTrue(Long id, Long userId);

    @Modifying
    @Query("""
            update NotificationHistory history
            set history.readAt = :readAt
            where history.user.id = :userId
              and history.success = true
              and history.readAt is null
            """)
    int markAllAsRead(Long userId, LocalDateTime readAt);
}
