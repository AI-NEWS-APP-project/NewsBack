package com.news.newsback.infra.ai;

import com.news.newsback.domain.news.model.News;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiServerClient 단위 테스트")
class AiServerClientTest {

    @InjectMocks
    private AiServerClient aiServerClient;

    @Mock
    private RestTemplate restTemplate;

    @Test
    @DisplayName("AI 서버 요청 실패 시 AiServerRequestException을 던진다")
    void AI_서버_요청_실패_시_예외를_던진다() {
        ReflectionTestUtils.setField(aiServerClient, "aiServerUrl", "http://ai-server");
        ReflectionTestUtils.setField(aiServerClient, "backendUrl", "http://backend");
        News news = News.create("제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, LocalDateTime.now());

        when(restTemplate.postForEntity(eq("http://ai-server/ai/cluster-id"), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> aiServerClient.requestClusterId(List.of(news)))
                .isInstanceOf(AiServerRequestException.class)
                .hasMessageContaining("/ai/cluster-id");
    }
}
