package com.news.newsback.presentation.controller.callback;

import com.news.newsback.application.news.NewsClusteringService;
import com.news.newsback.application.news.NewsSummaryService;
import com.news.newsback.infra.ai.AiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Hidden
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class AiCallbackController {

    private final NewsClusteringService newsClusteringService;
    private final NewsSummaryService newsSummaryService;

    @PostMapping("/cluster-id")
    public void clusterIdCallback(@RequestBody AiResponse.ClusterIdResponse response) {
        log.info("Received cluster-id callback: {}", response.getRequestId());
        newsClusteringService.updateClusterIds(response);
    }

    @PostMapping("/cluster-news")
    public void clusterNewsCallback(@RequestBody AiResponse.ClusterNewsSummaryResponse response) {
        log.info("Received cluster-news callback: {}", response.getRequestId());
        newsSummaryService.updateClusterNewsSummary(response);
    }

    @PostMapping("/keynews")
    public void keyNewsCallback(@RequestBody AiResponse.KeywordNewsResponse response) {
        log.info("Received keynews callback: {}", response.getRequestId());
        newsSummaryService.saveKeywordNews(response);
    }

    @PostMapping("/today-news")
    public void todayNewsCallback(@RequestBody AiResponse.TodayNewsResponse response) {
        log.info("Received today-news callback: {}", response.getRequestId());
        newsSummaryService.saveTodayNewsSummary(response);
    }
}
