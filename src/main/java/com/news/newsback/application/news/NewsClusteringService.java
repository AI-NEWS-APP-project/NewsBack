package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.ClusterNews;
import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.ClusterNewsRepository;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.infra.ai.AiClient;
import com.news.newsback.infra.ai.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsClusteringService {

    private final NewsRepository newsRepository;
    private final AiClient aiClient;
    private final ClusterNewsRepository clusterNewsRepository;
    private final NewsClusteringStatusService newsClusteringStatusService;

    public void processUnclusteredNews() {
        List<News> targets = newsClusteringStatusService.markUnclusteredNewsAsProcessing();
        if (targets.isEmpty()) {
            log.info("No unclustered news found.");
            return;
        }

        try {
            aiClient.requestClusterId(targets);
            log.info("Requested clustering for {} news", targets.size());
        } catch (Exception e) {
            log.error("Failed to request clustering from AI server", e);
            newsClusteringStatusService.markNewsAsError(targets);
        }
    }

    @Transactional
    public void updateClusterIds(AiResponse.ClusterIdResponse response) {
        if (!"success".equals(response.getStatus())) {
            log.error("AI clustering failed for request: {}", response.getRequestId());
            return;
        }

        for (AiResponse.ClusterIdResult result : response.getResults()) {
            newsRepository.findById(result.getRownewsId()).ifPresent(news -> {
                String clusterId = result.getClusterId();

                Optional<ClusterNews> clusterNews = clusterNewsRepository.findById(clusterId);
                if (clusterNews.isPresent()) {
                    clusterNews.get().incrementNewsCount();
                } else {
                    createClusterNews(clusterId);
                }

                news.updateClusterId(clusterId);
            });
        }
        log.info("Updated cluster IDs for {} news", response.getResults().size());
    }

    private ClusterNews createClusterNews(String clusterId) {
        ClusterNews clusterNews = ClusterNews.builder()
                .id(clusterId)
                .title("New Cluster " + clusterId)
                .newsCount(1)
                .build();
        return clusterNewsRepository.save(clusterNews);
    }
}
