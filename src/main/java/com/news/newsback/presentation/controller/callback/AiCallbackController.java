package com.news.newsback.presentation.controller.callback;

import com.news.newsback.application.news.AiCallbackProcessingService;
import com.news.newsback.infra.ai.AiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    private final AiCallbackProcessingService aiCallbackProcessingService;

    @PostMapping("/cluster-id")
    public ResponseEntity<Void> clusterIdCallback(@RequestBody AiResponse.ClusterIdResponse response) {
        log.info("Received cluster-id callback: {}", response.getRequestId());
        aiCallbackProcessingService.processClusterId(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cluster-news")
    public ResponseEntity<Void> clusterNewsCallback(@RequestBody AiResponse.ClusterNewsSummaryResponse response) {
        log.info("Received cluster-news callback: {}", response.getRequestId());
        aiCallbackProcessingService.processClusterNews(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/keynews")
    public ResponseEntity<Void> keyNewsCallback(@RequestBody AiResponse.KeywordNewsResponse response) {
        log.info("Received keynews callback: {}", response.getRequestId());
        aiCallbackProcessingService.processKeywordNews(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/today-news")
    public ResponseEntity<Void> todayNewsCallback(@RequestBody AiResponse.TodayNewsResponse response) {
        log.info("Received today-news callback: {}", response.getRequestId());
        aiCallbackProcessingService.processTodayNews(response);
        return ResponseEntity.ok().build();
    }
}
