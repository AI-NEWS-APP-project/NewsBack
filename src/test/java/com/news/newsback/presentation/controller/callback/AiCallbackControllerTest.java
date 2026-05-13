package com.news.newsback.presentation.controller.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.newsback.application.news.AiCallbackProcessingService;
import com.news.newsback.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiCallbackController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AiCallbackController 단위 테스트")
class AiCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiCallbackProcessingService aiCallbackProcessingService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("cluster-id callback은 비동기 처리로 위임하고 즉시 200을 반환한다")
    void cluster_id_callback_즉시_응답() throws Exception {
        mockMvc.perform(post("/callback/cluster-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "request-1",
                                  "status": "success",
                                  "results": []
                                }
                                """))
                .andExpect(status().isOk());

        verify(aiCallbackProcessingService).processClusterId(argThat(response ->
                response.getRequestId().equals("request-1")
        ));
    }

    @Test
    @DisplayName("keynews callback은 비동기 처리로 위임하고 즉시 200을 반환한다")
    void keynews_callback_즉시_응답() throws Exception {
        mockMvc.perform(post("/callback/keynews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "request-1",
                                  "status": "success",
                                  "keyword_id": 1,
                                  "keyword": "AI",
                                  "title": "AI 뉴스",
                                  "summary": "요약",
                                  "related_cluster_ids": [],
                                  "created_at": "2026-05-13T16:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        verify(aiCallbackProcessingService).processKeywordNews(argThat(response ->
                response.getRequestId().equals("request-1")
        ));
    }

    @Test
    @DisplayName("today-news callback은 비동기 처리로 위임하고 즉시 200을 반환한다")
    void today_news_callback_즉시_응답() throws Exception {
        mockMvc.perform(post("/callback/today-news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "request-1",
                                  "status": "success",
                                  "title": "최근 주요 뉴스 종합",
                                  "summary": "일일 요약",
                                  "news_ids": [101, 102],
                                  "news_count": 2,
                                  "generated_at": "2026-05-13T20:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        verify(aiCallbackProcessingService).processTodayNews(argThat(response ->
                response.getRequestId().equals("request-1")
                        && response.getStatus().equals("success")
                        && response.getNewsIds().contains("101")
                        && response.getNewsIds().contains("102")
        ));
    }
}
