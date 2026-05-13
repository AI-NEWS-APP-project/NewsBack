package com.news.newsback.infra.ai;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.news.model.ClusterNews;
import com.news.newsback.domain.news.model.News;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient implements AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${backend.url}")
    private String backendUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void requestClusterId(List<News> newsList) {
        String requestId = UUID.randomUUID().toString();
        String callbackUrl = buildCallbackUrl("/callback/cluster-id");

        List<AiRequest.RowNews> rowNewsList = newsList.stream()
                .map(this::toAiRowNews)
                .collect(Collectors.toList());

        AiRequest.ClusterIdRequest request = AiRequest.ClusterIdRequest.builder()
                .requestId(requestId)
                .callbackUrl(callbackUrl)
                .news(rowNewsList)
                .build();

        sendRequest("/ai/cluster-id", requestId, callbackUrl, request);
    }

    @Override
    public void requestClusterNewsSummary(ClusterNews clusterNews, List<News> newsList) {
        String requestId = UUID.randomUUID().toString();
        String callbackUrl = buildCallbackUrl("/callback/cluster-news");

        List<AiRequest.RowNews> rowNewsList = newsList.stream()
                .map(this::toAiRowNews)
                .collect(Collectors.toList());

        AiRequest.ClusterNewsSummaryRequest request = AiRequest.ClusterNewsSummaryRequest.builder()
                .requestId(requestId)
                .callbackUrl(callbackUrl)
                .clusterId(clusterNews.getId())
                .news(rowNewsList)
                .build();

        sendRequest("/ai/cluster-news", requestId, callbackUrl, request);
    }

    @Override
    public void requestKeywordNewsSummary(Keyword keyword, List<ClusterNews> clusterNewsList) {
        String requestId = UUID.randomUUID().toString();
        String callbackUrl = buildCallbackUrl("/callback/keynews");

        List<AiRequest.ClusterNewsItem> clusterItems = clusterNewsList.stream()
                .map(c -> AiRequest.ClusterNewsItem.builder()
                        .clusterId(c.getId())
                        .title(c.getTitle())
                        .summary(c.getRepresentativeSummary())
                        .build())
                .collect(Collectors.toList());

        AiRequest.KeywordNewsRequest request = AiRequest.KeywordNewsRequest.builder()
                .requestId(requestId)
                .callbackUrl(callbackUrl)
                .keywordId(keyword.getId())
                .keyword(keyword.getName())
                .clusterNews(clusterItems)
                .build();

        sendRequest("/ai/keynews", requestId, callbackUrl, request);
    }

    @Override
    public void requestTodayNewsSummary(int topKImportant, int topKLatest, int timeWindowHours) {
        String requestId = UUID.randomUUID().toString();
        String callbackUrl = buildCallbackUrl("/callback/today-news");

        AiRequest.TodayNewsRequest request = AiRequest.TodayNewsRequest.builder()
                .requestId(requestId)
                .callbackUrl(callbackUrl)
                .topKImportant(topKImportant)
                .topKLatest(topKLatest)
                .timeWindowHours(timeWindowHours)
                .build();

        sendRequest("/ai/today-news", requestId, callbackUrl, request);
    }

    private void sendRequest(String path, String requestId, String callbackUrl, Object request) {
        try {
            restTemplate.postForEntity(aiServerUrl + path, request, Void.class);
            log.info("Sent request to AI server. path={}, requestId={}, callbackUrl={}", path, requestId, callbackUrl);
        } catch (Exception e) {
            log.error("Failed to send request to AI server. path={}, requestId={}, callbackUrl={}", path, requestId, callbackUrl, e);
            throw new AiServerRequestException("AI server request failed: " + path, e);
        }
    }

    private String buildCallbackUrl(String callbackPath) {
        return trimTrailingSlash(backendUrl) + normalizePath(contextPath) + callbackPath;
    }

    private String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private AiRequest.RowNews toAiRowNews(News news) {
        return AiRequest.RowNews.builder()
                .rownewsId(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .createdAt(news.getPublishedAt().toString())
                .url(news.getUrl())
                .build();
    }
}
