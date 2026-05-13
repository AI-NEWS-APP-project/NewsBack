package com.news.newsback.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchedulingConfig 단위 테스트")
class SchedulingConfigTest {

    @Test
    @DisplayName("스케줄러 전용 thread pool을 설정한다")
    void taskScheduler_설정() {
        SchedulingConfig config = new SchedulingConfig();

        TaskScheduler taskScheduler = config.taskScheduler();

        assertThat(taskScheduler).isInstanceOf(ThreadPoolTaskScheduler.class);
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) taskScheduler;
        assertThat(scheduler.getScheduledThreadPoolExecutor().getCorePoolSize()).isEqualTo(4);
        assertThat(scheduler.getThreadNamePrefix()).isEqualTo("scheduler-");
        scheduler.shutdown();
    }
}
