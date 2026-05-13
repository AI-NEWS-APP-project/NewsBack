package com.news.newsback.domain.scheduler.repository;

import com.news.newsback.domain.scheduler.model.SchedulerErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerErrorLogRepository extends JpaRepository<SchedulerErrorLog, Long> {
}
