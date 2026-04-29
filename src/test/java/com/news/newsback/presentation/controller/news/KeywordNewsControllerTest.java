package com.news.newsback.presentation.controller.news;

import com.news.newsback.application.news.KeywordNewsService;
import com.news.newsback.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KeywordNewsController.class)
@Import(TestSecurityConfig.class)
@DisplayName("KeywordNewsController 단위 테스트")
class KeywordNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeywordNewsService keywordNewsService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    @DisplayName("키워드별 최신 요약 리스트를 조회한다")
    void 최신_요약_리스트_조회() throws Exception {
        when(keywordNewsService.getLatestKeywordNews()).thenReturn(List.of(keywordNewsResponse()));

        mockMvc.perform(get("/api/news/keyword-news/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].keywordName").value("경제"))
                .andExpect(jsonPath("$.data[0].summaryText").value("경제 요약"));

        verify(keywordNewsService).getLatestKeywordNews();
    }

    @Test
    @WithMockUser
    @DisplayName("특정 키워드 요약 히스토리를 페이지로 조회한다")
    void 요약_히스토리_조회() throws Exception {
        when(keywordNewsService.getKeywordNewsHistory(org.mockito.Mockito.eq(1L), org.mockito.Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(keywordNewsResponse())));

        mockMvc.perform(get("/api/news/keyword-news")
                        .param("keywordId", "1")
                        .param("page", "0")
                        .param("size", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].keywordName").value("경제"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(keywordNewsService).getKeywordNewsHistory(org.mockito.Mockito.eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(6);
    }

    @Test
    @WithMockUser
    @DisplayName("요약 상세를 조회한다")
    void 요약_상세_조회() throws Exception {
        when(keywordNewsService.getKeywordNewsDetail(1L)).thenReturn(keywordNewsDetailResponse());

        mockMvc.perform(get("/api/news/keyword-news/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.links[0].title").value("기사 1"))
                .andExpect(jsonPath("$.data.links[0].url").value("https://news.example.com/1"));

        verify(keywordNewsService).getKeywordNewsDetail(1L);
    }

    private KeywordNewsResponse.Summary keywordNewsResponse() {
        return KeywordNewsResponse.Summary.builder()
                .id(1L)
                .keywordId(10L)
                .keywordName("경제")
                .summaryText("경제 요약")
                .clusterNewsCount(3)
                .createdAt(LocalDateTime.of(2026, 4, 28, 10, 0))
                .build();
    }

    private KeywordNewsResponse.Detail keywordNewsDetailResponse() {
        return KeywordNewsResponse.Detail.builder()
                .id(1L)
                .keywordId(10L)
                .keywordName("경제")
                .summaryText("경제 요약")
                .clusterNewsCount(3)
                .createdAt(LocalDateTime.of(2026, 4, 28, 10, 0))
                .links(List.of(KeywordNewsResponse.Link.builder()
                        .title("기사 1")
                        .url("https://news.example.com/1")
                        .build()))
                .build();
    }

}
