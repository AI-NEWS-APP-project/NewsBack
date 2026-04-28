package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsClusteringStatusService 단위 테스트")
class NewsClusteringStatusServiceTest {

    @InjectMocks
    private NewsClusteringStatusService newsClusteringStatusService;

    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("클러스터링되지 않은 뉴스를 PROCESSING 상태로 변경한다")
    void 미클러스터링_뉴스_PROCESSING_변경() {
        News news = news("https://news.example.com/1");
        when(newsRepository.findAllByClusterIdIsNull()).thenReturn(List.of(news));

        List<News> result = newsClusteringStatusService.markUnclusteredNewsAsProcessing();

        assertThat(result).containsExactly(news);
        assertThat(news.getProcessingStatus()).isEqualTo(News.ProcessingStatus.PROCESSING);
    }

    @Test
    @DisplayName("전달받은 뉴스 목록을 다시 조회해 ERROR 상태로 변경한다")
    void 뉴스_ERROR_변경() {
        News requestedNews = news("https://news.example.com/1");
        News managedNews = news("https://news.example.com/1");
        when(newsRepository.findAllById(List.of(requestedNews.getId()))).thenReturn(List.of(managedNews));

        newsClusteringStatusService.markNewsAsError(List.of(requestedNews));

        assertThat(managedNews.getProcessingStatus()).isEqualTo(News.ProcessingStatus.ERROR);
    }

    private News news(String url) {
        return News.create("제목", "본문", url, "언론사", null, "ko", "KR", null, LocalDateTime.now());
    }
}
