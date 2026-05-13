package com.news.newsback.application.news;

import com.news.newsback.infra.ai.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCallbackProcessingService {

    private final NewsClusteringService newsClusteringService;
    private final NewsSummaryService newsSummaryService;

    @Async("aiCallbackTaskExecutor")
    public void processClusterId(AiResponse.ClusterIdResponse response) {
        try {
            newsClusteringService.updateClusterIds(response);
        } catch (Exception e) {
            log.error("Failed to process cluster-id callback asynchronously. requestId={}", response.getRequestId(), e);
        }
    }

    @Async("aiCallbackTaskExecutor")
    public void processClusterNews(AiResponse.ClusterNewsSummaryResponse response) {
        try {
            newsSummaryService.updateClusterNewsSummary(response);
        } catch (Exception e) {
            log.error("Failed to process cluster-news callback asynchronously. requestId={}", response.getRequestId(), e);
        }
    }

    @Async("aiCallbackTaskExecutor")
    public void processKeywordNews(AiResponse.KeywordNewsResponse response) {
        try {
            newsSummaryService.saveKeywordNews(response);
        } catch (Exception e) {
            log.error("Failed to process keynews callback asynchronously. requestId={}", response.getRequestId(), e);
        }
    }

    @Async("aiCallbackTaskExecutor")
    public void processTodayNews(AiResponse.TodayNewsResponse response) {
        try {
            newsSummaryService.saveTodayNewsSummary(response);
        } catch (Exception e) {
            log.error("Failed to process today-news callback asynchronously. requestId={}", response.getRequestId(), e);
        }
    }
}
