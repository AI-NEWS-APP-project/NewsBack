package com.news.newsback.application.news;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.news.repository.KeywordNewsRepository;
import com.news.newsback.presentation.controller.news.KeywordNewsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KeywordNewsService 단위 테스트")
class KeywordNewsServiceTest {

    @InjectMocks
    private KeywordNewsService keywordNewsService;

    @Mock
    private KeywordNewsRepository keywordNewsRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("최신 키워드 뉴스 요약을 최신순으로 조회한다")
    void 최신_키워드_뉴스_조회() {
        KeywordNews keywordNews = keywordNews(1L, keyword(10L, "경제"), "경제 요약", 3, LocalDateTime.now());
        when(keywordNewsRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(keywordNews));

        List<KeywordNewsResponse.Summary> result = keywordNewsService.getLatestKeywordNews();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getKeywordId()).isEqualTo(10L);
        assertThat(result.get(0).getKeywordName()).isEqualTo("경제");
        assertThat(result.get(0).getSummaryText()).isEqualTo("경제 요약");
        assertThat(result.get(0).getClusterNewsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("특정 키워드의 뉴스 요약 히스토리를 페이지로 조회한다")
    void 키워드_뉴스_히스토리_조회() {
        PageRequest pageable = PageRequest.of(0, 6);
        KeywordNews keywordNews = keywordNews(1L, keyword(10L, "AI"), "AI 요약", 2, LocalDateTime.now());
        when(keywordRepository.existsById(10L)).thenReturn(true);
        when(keywordNewsRepository.findByKeywordIdOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(keywordNews), pageable, 1));

        Page<KeywordNewsResponse.Summary> result = keywordNewsService.getKeywordNewsHistory(10L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getKeywordName()).isEqualTo("AI");
    }

    @Test
    @DisplayName("존재하지 않는 키워드의 뉴스 요약 히스토리 조회 시 NewsException을 던진다")
    void 존재하지_않는_키워드_히스토리_조회() {
        PageRequest pageable = PageRequest.of(0, 6);
        when(keywordRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> keywordNewsService.getKeywordNewsHistory(99L, pageable))
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
    }

    @Test
    @DisplayName("키워드 뉴스 상세를 링크와 함께 조회한다")
    void 키워드_뉴스_상세_조회() {
        KeywordNews keywordNews = keywordNews(1L, keyword(10L, "정치"), "정치 요약", 2, LocalDateTime.now());
        keywordNews.addLink("https://news.example.com/1", "기사 1");
        when(keywordNewsRepository.findById(1L)).thenReturn(Optional.of(keywordNews));

        KeywordNewsResponse.Detail result = keywordNewsService.getKeywordNewsDetail(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLinks()).hasSize(1);
        assertThat(result.getLinks().get(0).getTitle()).isEqualTo("기사 1");
        assertThat(result.getLinks().get(0).getUrl()).isEqualTo("https://news.example.com/1");
    }

    @Test
    @DisplayName("키워드 뉴스가 없으면 NewsException을 던진다")
    void 키워드_뉴스_상세_없음() {
        when(keywordNewsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> keywordNewsService.getKeywordNewsDetail(99L))
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
    }

    private Keyword keyword(Long id, String name) {
        Keyword keyword = Keyword.builder()
                .name(name)
                .build();
        keyword.setId(id);
        return keyword;
    }

    private KeywordNews keywordNews(Long id, Keyword keyword, String summaryText, int clusterNewsCount, LocalDateTime createdAt) {
        KeywordNews keywordNews = KeywordNews.builder()
                .id(id)
                .keyword(keyword)
                .summaryText(summaryText)
                .clusterNewsCount(clusterNewsCount)
                .build();
        ReflectionTestUtils.setField(keywordNews, "createdAt", createdAt);
        return keywordNews;
    }
}
