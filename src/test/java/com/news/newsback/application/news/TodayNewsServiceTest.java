package com.news.newsback.application.news;

import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.model.TodayNewsSummary;
import com.news.newsback.domain.news.repository.TodayNewsSummaryRepository;
import com.news.newsback.presentation.controller.news.TodayNewsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodayNewsService 단위 테스트")
class TodayNewsServiceTest {

    @InjectMocks
    private TodayNewsService todayNewsService;

    @Mock
    private TodayNewsSummaryRepository todayNewsSummaryRepository;

    @Test
    @DisplayName("최신 일일 뉴스 요약은 원본 뉴스 없이 조회한다")
    void 최신_일일_뉴스_요약_조회() {
        News news = News.create("기사 제목", "기사 본문", "https://news.example.com/1", "언론사", null, "ko", "KR", null,
                LocalDateTime.of(2026, 5, 1, 10, 0));
        TodayNewsSummary summary = TodayNewsSummary.builder()
                .title("최근 주요 뉴스 종합")
                .summary("오늘의 주요 뉴스 요약")
                .build();
        ReflectionTestUtils.setField(summary, "id", 1L);
        ReflectionTestUtils.setField(summary, "generatedAt", LocalDateTime.of(2026, 5, 1, 20, 0));
        summary.addNews(news);

        when(todayNewsSummaryRepository.findFirstByOrderByGeneratedAtDesc()).thenReturn(Optional.of(summary));

        TodayNewsResponse.Summary result = todayNewsService.getLatestTodayNewsSummary();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("최근 주요 뉴스 종합");
        assertThat(result.getSummary()).isEqualTo("오늘의 주요 뉴스 요약");
        assertThat(result.getNewsCount()).isEqualTo(1);
        assertThat(result.getGeneratedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 20, 0));
    }

    @Test
    @DisplayName("일일 뉴스 요약 상세는 원본 뉴스 링크를 포함해 조회한다")
    void 일일_뉴스_요약_상세_조회() {
        News news = News.create("기사 제목", "기사 본문", "https://news.example.com/1", "언론사", null, "ko", "KR", null,
                LocalDateTime.of(2026, 5, 1, 10, 0));
        TodayNewsSummary summary = TodayNewsSummary.builder()
                .title("최근 주요 뉴스 종합")
                .summary("오늘의 주요 뉴스 요약")
                .build();
        ReflectionTestUtils.setField(summary, "id", 1L);
        ReflectionTestUtils.setField(summary, "generatedAt", LocalDateTime.of(2026, 5, 1, 20, 0));
        summary.addNews(news);

        when(todayNewsSummaryRepository.findWithSummaryNewsById(1L)).thenReturn(Optional.of(summary));

        TodayNewsResponse.Detail result = todayNewsService.getTodayNewsSummaryDetail(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNews()).hasSize(1);
        assertThat(result.getNews().get(0).getTitle()).isEqualTo("기사 제목");
        assertThat(result.getNews().get(0).getUrl()).isEqualTo("https://news.example.com/1");
    }

    @Test
    @DisplayName("일일 뉴스 요약이 없으면 NewsException을 던진다")
    void 일일_뉴스_요약_없음() {
        when(todayNewsSummaryRepository.findFirstByOrderByGeneratedAtDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todayNewsService.getLatestTodayNewsSummary())
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
    }

    @Test
    @DisplayName("일일 뉴스 요약 상세가 없으면 NewsException을 던진다")
    void 일일_뉴스_요약_상세_없음() {
        when(todayNewsSummaryRepository.findWithSummaryNewsById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todayNewsService.getTodayNewsSummaryDetail(99L))
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
    }
}
