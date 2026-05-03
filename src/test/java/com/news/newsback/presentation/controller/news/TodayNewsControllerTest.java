package com.news.newsback.presentation.controller.news;

import com.news.newsback.application.news.TodayNewsService;
import com.news.newsback.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodayNewsController.class)
@Import(TestSecurityConfig.class)
@DisplayName("TodayNewsController 단위 테스트")
class TodayNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodayNewsService todayNewsService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    @DisplayName("최신 일일 통합 브리핑을 조회한다")
    void 최신_일일_통합_브리핑_조회() throws Exception {
        when(todayNewsService.getLatestTodayNewsSummary()).thenReturn(todayNewsResponse());

        mockMvc.perform(get("/api/news/daily-briefings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("최근 주요 뉴스 종합"))
                .andExpect(jsonPath("$.data.summary").value("오늘의 주요 뉴스 요약"))
                .andExpect(jsonPath("$.data.newsCount").value(1))
                .andExpect(jsonPath("$.data.news[0].title").value("기사 제목"))
                .andExpect(jsonPath("$.data.news[0].url").value("https://news.example.com/1"));

        verify(todayNewsService).getLatestTodayNewsSummary();
    }

    private TodayNewsResponse.Detail todayNewsResponse() {
        return TodayNewsResponse.Detail.builder()
                .id(1L)
                .title("최근 주요 뉴스 종합")
                .summary("오늘의 주요 뉴스 요약")
                .newsCount(1)
                .generatedAt(LocalDateTime.of(2026, 5, 1, 20, 0))
                .news(List.of(TodayNewsResponse.NewsItem.builder()
                        .id("news-1")
                        .title("기사 제목")
                        .url("https://news.example.com/1")
                        .source("언론사")
                        .publishedAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                        .build()))
                .build();
    }
}
