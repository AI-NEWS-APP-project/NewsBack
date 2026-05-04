package com.news.newsback.domain.alarm.repository;

import com.news.newsback.domain.alarm.model.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    boolean existsByUserIdAndKeywordNewsId(Long userId, Long keywordNewsId);
}
