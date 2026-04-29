package com.news.newsback.unit.domain.news;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.domain.news.service.NewsValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsValidator 단위 테스트")
class NewsValidatorTest {

    @InjectMocks
    private NewsValidator newsValidator;

    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("URL이 중복되면 true 반환")
    void URL_중복_검증_테스트() {
        News news = News.create("Title", "Content", "https://url.com", "Source", null, "ko", "KR", null, LocalDateTime.now());

        when(newsRepository.existsByUrl(news.getUrl())).thenReturn(true);
        assertThat(newsValidator.isDuplicate(news)).isTrue();

        when(newsRepository.existsByUrl(news.getUrl())).thenReturn(false);
        assertThat(newsValidator.isDuplicate(news)).isFalse();
    }
}
