package com.news.newsback.domain.keyword.api;

import com.news.newsback.config.TestSecurityConfig;
import com.news.newsback.domain.keyword.application.KeywordService;
import com.news.newsback.domain.keyword.dto.KeywordDto;
import com.news.newsback.global.security.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KeywordController.class)
@Import(TestSecurityConfig.class)
@DisplayName("KeywordController 단위 테스트")
class KeywordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeywordService keywordService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("인증 사용자 기준으로 키워드 목록을 조회한다")
    void 키워드_목록_조회() throws Exception {
        when(keywordService.retrieveKeywordsByUserId(12L)).thenReturn(List.of(new KeywordDto(1L, "AI")));

        mockMvc.perform(get("/keywords")
                        .with(authentication(authenticatedUser(12L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("AI"));

        verify(keywordService).retrieveKeywordsByUserId(12L);
    }

    @Test
    @DisplayName("인증 사용자 기준으로 키워드를 추가한다")
    void 키워드_추가() throws Exception {
        mockMvc.perform(post("/keywords")
                        .with(authentication(authenticatedUser(12L)))
                        .param("keyword", "AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(keywordService).registerKeyword(12L, "AI");
    }

    @Test
    @DisplayName("인증 사용자 기준으로 키워드를 일괄 추가한다")
    void 키워드_일괄_추가() throws Exception {
        mockMvc.perform(post("/keywords/bulk")
                        .with(authentication(authenticatedUser(12L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                ["AI", "반도체"]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(keywordService).registerKeyword(12L, "AI");
        verify(keywordService).registerKeyword(12L, "반도체");
    }

    @Test
    @DisplayName("인증 사용자 기준으로 키워드 구독을 취소한다")
    void 키워드_구독_취소() throws Exception {
        mockMvc.perform(delete("/keywords/1")
                        .with(authentication(authenticatedUser(12L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(keywordService).unsubscribeKeyword(12L, 1L);
    }

    @Test
    @DisplayName("인기 키워드를 조회한다")
    void 인기_키워드_조회() throws Exception {
        when(keywordService.retrievePopularKeywords()).thenReturn(List.of("AI", "반도체"));

        mockMvc.perform(get("/keywords/popular")
                        .with(authentication(authenticatedUser(12L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("AI"));

        verify(keywordService).retrievePopularKeywords();
    }

    private UsernamePasswordAuthenticationToken authenticatedUser(Long userId) {
        return new UsernamePasswordAuthenticationToken(new AuthenticatedUser(userId), null, List.of());
    }
}
