package com.news.newsback.application.news;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.keyword.util.KeywordNormalizer;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.model.*;
import com.news.newsback.domain.news.repository.*;
import com.news.newsback.infra.ai.AiClient;
import com.news.newsback.infra.ai.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSummaryService {

    private static final int CLUSTER_SUMMARY_NEWS_INCREMENT_THRESHOLD = 5;

    private final KeywordRepository keywordRepository;
    private final ClusterNewsRepository clusterNewsRepository;
    private final NewsRepository newsRepository;
    private final KeywordNewsRepository keywordNewsRepository;
    private final TodayNewsSummaryRepository todayNewsSummaryRepository;

    private final AiClient aiClient;

    public void requestClusterSummaries() {
        List<ClusterNews> targets = clusterNewsRepository.findAllRequiringSummary(CLUSTER_SUMMARY_NEWS_INCREMENT_THRESHOLD);
        for (ClusterNews cluster : targets) {
            List<News> clusterNewsList = newsRepository.findAllByClusterIdOrderByPublishedAtDesc(cluster.getId());
            if (!clusterNewsList.isEmpty()) {
                aiClient.requestClusterNewsSummary(cluster, clusterNewsList);
                log.info("Requested summary for cluster: {}", cluster.getId());
            }
        }
    }

    public void requestKeywordSummaries() {
        List<Keyword> keywords = keywordRepository.findAll();
        if (keywords.isEmpty()) {
            log.info("No keywords found for keyword news summary.");
            return;
        }

        List<SearchableClusterNews> searchableClusters = clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull()
                .stream()
                .map(SearchableClusterNews::from)
                .toList();
        if (searchableClusters.isEmpty()) {
            log.info("No summarized clusters found for keyword news summary.");
            return;
        }

        for (Keyword keyword : keywords) {
            String normalizedKeyword = KeywordNormalizer.normalize(keyword.getName());
            if (normalizedKeyword.isBlank()) {
                continue;
            }

            List<ClusterNews> matchedClusters = searchableClusters.stream()
                    .filter(cluster -> cluster.contains(normalizedKeyword))
                    .map(SearchableClusterNews::clusterNews)
                    .toList();

            if (!matchedClusters.isEmpty()) {
                aiClient.requestKeywordNewsSummary(keyword, matchedClusters);
                log.info("Requested summary for keyword: {}", keyword.getName());
            }
        }
    }

    public void requestTodaySummary() {
        aiClient.requestTodayNewsSummary(10, 10, 24);
        log.info("Requested today news summary");
    }

    @Transactional
    public void updateClusterNewsSummary(AiResponse.ClusterNewsSummaryResponse response) {
        if (!isSuccess(response.getStatus())) {
            log.info("AI cluster news summary skipped. cluster={}, status={}", response.getClusterId(), response.getStatus());
            return;
        }

        clusterNewsRepository.findById(response.getClusterId()).ifPresent(clusterNews -> {
            clusterNews.updateSummary(response.getTitle(), response.getSummary());
            log.info("Updated summary for cluster: {}", clusterNews.getId());
        });
    }

    @Transactional
    public void saveKeywordNews(AiResponse.KeywordNewsResponse response) {
        if (!isSuccess(response.getStatus())) {
            log.info("AI keyword news summary skipped. keyword={}, status={}", response.getKeywordId(), response.getStatus());
            return;
        }

        Keyword keyword = keywordRepository.findById(response.getKeywordId())
                .orElseThrow(() -> new NewsException(NewsErrorCode.KEYWORD_NOT_FOUND));

        KeywordNews keywordNews = KeywordNews.builder()
                .keyword(keyword)
                .summaryText(response.getSummary())
                .clusterNewsCount(response.getRelatedClusterIds().size())
                .build();

        // 1. 대표 뉴스 링크 매칭 (각 클러스터당 최신 뉴스 1개씩)
        for (String clusterId : response.getRelatedClusterIds()) {
            List<News> clusterNewsList = newsRepository.findAllByClusterIdOrderByPublishedAtDesc(clusterId);
            if (!clusterNewsList.isEmpty()) {
                News representativeNews = clusterNewsList.get(0);
                keywordNews.addLink(representativeNews.getUrl(), representativeNews.getTitle());
            }
        }

        keywordNewsRepository.save(keywordNews);
        log.info("Saved keyword news summary for: {}", keyword.getName());
    }

    @Transactional
    public void saveTodayNewsSummary(AiResponse.TodayNewsResponse response) {
        if (!isSuccess(response.getStatus())) {
            log.info("AI today news summary skipped. status={}", response.getStatus());
            return;
        }

        TodayNewsSummary summary = TodayNewsSummary.builder()
                .title(response.getTitle())
                .summary(response.getSummary())
                .newsCount(response.getNewsCount())
                .build();

        // 모든 news_ids 매칭
        for (String newsId : response.getNewsIds()) {
            newsRepository.findById(newsId).ifPresent(summary::addNews);
        }

        todayNewsSummaryRepository.save(summary);
        log.info("Saved today news summary: {}", summary.getTitle());
    }

    private record SearchableClusterNews(ClusterNews clusterNews, String searchableText) {

        private static SearchableClusterNews from(ClusterNews clusterNews) {
            String searchableText = Stream.of(clusterNews.getTitle(), clusterNews.getRepresentativeSummary())
                    .map(KeywordNormalizer::normalize)
                    .collect(Collectors.joining(" "));
            return new SearchableClusterNews(clusterNews, searchableText);
        }

        private boolean contains(String normalizedKeyword) {
            return searchableText.contains(normalizedKeyword);
        }
    }

    private boolean isSuccess(String status) {
        return "success".equalsIgnoreCase(status);
    }
}
