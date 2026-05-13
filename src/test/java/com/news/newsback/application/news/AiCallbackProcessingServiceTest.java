package com.news.newsback.application.news;

import com.news.newsback.application.scheduler.SchedulerErrorLogService;
import com.news.newsback.infra.ai.AiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiCallbackProcessingService 단위 테스트")
class AiCallbackProcessingServiceTest {

    @InjectMocks
    private AiCallbackProcessingService aiCallbackProcessingService;

    @Mock
    private NewsClusteringService newsClusteringService;

    @Mock
    private NewsSummaryService newsSummaryService;

    @Mock
    private SchedulerErrorLogService schedulerErrorLogService;

    @Test
    @DisplayName("today-news callback 성공은 일일 뉴스 요약 저장으로 위임한다")
    void today_news_callback_성공_저장_위임() {
        AiResponse.TodayNewsResponse response = new AiResponse.TodayNewsResponse(
                "request-1", "success", "제목", "요약", List.of("news-1"), 1, "2026-05-13T20:00:00");

        aiCallbackProcessingService.processTodayNews(response);

        verify(newsSummaryService).saveTodayNewsSummary(response);
        verifyNoInteractions(schedulerErrorLogService);
    }

    @Test
    @DisplayName("today-news callback 비성공은 저장하지 않고 에러 로그를 남긴다")
    void today_news_callback_비성공_로그_저장() {
        AiResponse.TodayNewsResponse response = new AiResponse.TodayNewsResponse(
                "request-1", "fail", null, null, List.of(), 0, "2026-05-13T20:00:00");

        aiCallbackProcessingService.processTodayNews(response);

        ArgumentCaptor<Exception> errorCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(newsSummaryService, never()).saveTodayNewsSummary(response);
        verify(schedulerErrorLogService).record(
                eq("AiCallbackProcessingService"),
                eq("processTodayNews"),
                errorCaptor.capture(),
                eq("requestId=request-1")
        );
        assertThat(errorCaptor.getValue().getMessage()).contains("status=fail");
    }

    @Test
    @DisplayName("today-news callback 처리 예외는 에러 로그를 남긴다")
    void today_news_callback_예외_로그_저장() {
        AiResponse.TodayNewsResponse response = new AiResponse.TodayNewsResponse(
                "request-1", "success", "제목", "요약", List.of("news-1"), 1, "2026-05-13T20:00:00");
        IllegalStateException error = new IllegalStateException("save failed");
        doThrow(error).when(newsSummaryService).saveTodayNewsSummary(response);

        aiCallbackProcessingService.processTodayNews(response);

        verify(schedulerErrorLogService).record(
                "AiCallbackProcessingService",
                "processTodayNews",
                error,
                "requestId=request-1"
        );
    }
}
