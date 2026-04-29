package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.ClusterNews;
import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.ClusterNewsRepository;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.infra.ai.AiResponse;
import com.news.newsback.infra.ai.AiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsClusteringService 단위 테스트")
class NewsClusteringServiceTest {

    @InjectMocks
    private NewsClusteringService newsClusteringService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private AiClient aiClient;

    @Mock
    private ClusterNewsRepository clusterNewsRepository;

    @Mock
    private NewsClusteringStatusService newsClusteringStatusService;

    @Test
    @DisplayName("AI 클러스터링 요청 실패 시 대상 뉴스를 ERROR 상태로 변경하도록 위임한다")
    void AI_클러스터링_요청_실패_시_ERROR_상태_변경_위임() {
        News news = News.create("제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, LocalDateTime.now());
        List<News> targets = List.of(news);

        when(newsClusteringStatusService.markUnclusteredNewsAsProcessing()).thenReturn(targets);
        doThrow(new RuntimeException("AI server unavailable"))
                .when(aiClient)
                .requestClusterId(targets);

        newsClusteringService.processUnclusteredNews();

        verify(newsClusteringStatusService).markNewsAsError(targets);
    }

    @Test
    @DisplayName("클러스터 ID callback에서 기존 클러스터가 있으면 뉴스 개수만 증가시킨다")
    void 기존_클러스터가_있으면_뉴스_개수_증가() {
        News news = News.create("제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, LocalDateTime.now());
        ClusterNews clusterNews = ClusterNews.builder()
                .id("cluster-1")
                .newsCount(1)
                .build();
        AiResponse.ClusterIdResponse response = new AiResponse.ClusterIdResponse(
                "request-id", "success", List.of(new AiResponse.ClusterIdResult(news.getId(), "cluster-1")), "2026-04-28T10:00:00");

        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(clusterNewsRepository.findById("cluster-1")).thenReturn(Optional.of(clusterNews));

        newsClusteringService.updateClusterIds(response);

        assertThat(clusterNews.getNewsCount()).isEqualTo(2);
        assertThat(news.getClusterId()).isEqualTo("cluster-1");
        assertThat(news.getProcessingStatus()).isEqualTo(News.ProcessingStatus.COMPLETED);
        verify(clusterNewsRepository, never()).save(clusterNews);
    }

    @Test
    @DisplayName("클러스터 ID callback에서 클러스터가 없으면 새 클러스터를 저장한다")
    void 클러스터가_없으면_새_클러스터_저장() {
        News news = News.create("제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, LocalDateTime.now());
        AiResponse.ClusterIdResponse response = new AiResponse.ClusterIdResponse(
                "request-id", "success", List.of(new AiResponse.ClusterIdResult(news.getId(), "cluster-1")), "2026-04-28T10:00:00");

        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(clusterNewsRepository.findById("cluster-1")).thenReturn(Optional.empty());

        newsClusteringService.updateClusterIds(response);

        assertThat(news.getClusterId()).isEqualTo("cluster-1");
        verify(clusterNewsRepository).save(org.mockito.Mockito.argThat(clusterNews ->
                clusterNews.getId().equals("cluster-1") && clusterNews.getNewsCount().equals(1)));
    }
}
