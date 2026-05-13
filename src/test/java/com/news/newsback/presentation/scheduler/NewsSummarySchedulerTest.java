package com.news.newsback.presentation.scheduler;

import com.news.newsback.application.news.NewsClusteringService;
import com.news.newsback.application.news.NewsGatheringService;
import com.news.newsback.application.news.NewsSummaryService;
import com.news.newsback.application.scheduler.SchedulerErrorLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsSummaryScheduler 단위 테스트")
class NewsSummarySchedulerTest {

    @InjectMocks
    private NewsSummaryScheduler newsSummaryScheduler;

    @Mock
    private NewsGatheringService newsGatheringService;

    @Mock
    private NewsClusteringService newsClusteringService;

    @Mock
    private NewsSummaryService newsSummaryService;

    @Mock
    private SchedulerErrorLogService schedulerErrorLogService;

    @Test
    @DisplayName("클러스터링 요청 실패 시 스케줄러 에러 로그를 저장한다")
    void 클러스터링_요청_실패_로그_저장() {
        IllegalStateException error = new IllegalStateException("clustering failed");
        doThrow(error).when(newsClusteringService).processUnclusteredNews();

        newsSummaryScheduler.runNewsGatheringAndClustering();

        verify(schedulerErrorLogService).record(
                eq("NewsSummaryScheduler"),
                eq("processUnclusteredNews"),
                same(error),
                isNull()
        );
    }

    @Test
    @DisplayName("일일 뉴스 요약 요청 실패 시 스케줄러 에러 로그를 저장한다")
    void 일일_뉴스_요약_요청_실패_로그_저장() {
        IllegalStateException error = new IllegalStateException("today summary failed");
        doThrow(error).when(newsSummaryService).requestTodaySummary();

        newsSummaryScheduler.runTodayNewsSummary();

        verify(schedulerErrorLogService).record(
                eq("NewsSummaryScheduler"),
                eq("requestTodaySummary"),
                same(error),
                isNull()
        );
    }
}
