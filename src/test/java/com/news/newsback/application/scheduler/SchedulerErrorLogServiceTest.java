package com.news.newsback.application.scheduler;

import com.news.newsback.domain.scheduler.model.SchedulerErrorLog;
import com.news.newsback.domain.scheduler.repository.SchedulerErrorLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulerErrorLogService 단위 테스트")
class SchedulerErrorLogServiceTest {

    @InjectMocks
    private SchedulerErrorLogService schedulerErrorLogService;

    @Mock
    private SchedulerErrorLogRepository schedulerErrorLogRepository;

    @Test
    @DisplayName("스케줄러 에러 정보를 저장한다")
    void 스케줄러_에러_저장() {
        IllegalStateException error = new IllegalStateException("AI 서버 요청 실패");

        schedulerErrorLogService.record("NewsSummaryScheduler", "requestKeywordSummaries", error, "keyword=AI");

        ArgumentCaptor<SchedulerErrorLog> captor = ArgumentCaptor.forClass(SchedulerErrorLog.class);
        verify(schedulerErrorLogRepository).save(captor.capture());
        SchedulerErrorLog savedLog = captor.getValue();
        assertThat(savedLog.getSchedulerName()).isEqualTo("NewsSummaryScheduler");
        assertThat(savedLog.getMethodName()).isEqualTo("requestKeywordSummaries");
        assertThat(savedLog.getErrorType()).isEqualTo(IllegalStateException.class.getName());
        assertThat(savedLog.getErrorMessage()).isEqualTo("AI 서버 요청 실패");
        assertThat(savedLog.getContext()).isEqualTo("keyword=AI");
        assertThat(savedLog.getOccurredAt()).isNotNull();
        assertThat(savedLog.getStackTrace()).contains("IllegalStateException");
    }
}
