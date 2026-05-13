package com.news.newsback.application.scheduler;

import com.news.newsback.domain.scheduler.model.SchedulerErrorLog;
import com.news.newsback.domain.scheduler.repository.SchedulerErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerErrorLogService {

    private final SchedulerErrorLogRepository schedulerErrorLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String schedulerName, String methodName, Throwable error, String context) {
        try {
            schedulerErrorLogRepository.save(SchedulerErrorLog.create(schedulerName, methodName, error, context));
        } catch (Exception loggingError) {
            log.error("Failed to save scheduler error log. scheduler={}, method={}", schedulerName, methodName, loggingError);
        }
    }
}
