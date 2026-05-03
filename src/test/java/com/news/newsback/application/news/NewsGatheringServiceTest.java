package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.*;
import com.news.newsback.domain.news.exception.*;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.domain.news.service.NewsValidator;
import com.news.newsback.infra.rss.RssParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsGatheringService 단위 테스트")
class NewsGatheringServiceTest {

    @InjectMocks
    private NewsGatheringService newsGatheringService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsValidator newsValidator;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RssParser rssParser;

    @Test
    @DisplayName("RSS 피드 가져오기 실패 시 NewsException 발생")
    void RSS_수집_실패_테스트() {
        NewsSource source = new NewsSource("Test", "Type", "https://invalid.com/rss", "ko", "KR");
        when(restTemplate.getForObject(source.url(), byte[].class)).thenReturn(null);

        assertThatThrownBy(() -> newsGatheringService.gatherNewsFromRss(source))
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.RSS_FETCH_FAILED);
    }

    @Test
    @DisplayName("수집된 뉴스 중복 확인 및 저장")
    void 뉴스_수집_및_중복_검증_저장_테스트() {
        NewsSource source = new NewsSource("Test", "Type", "https://example.com/rss", "ko", "KR");
        String xml = "<rss>...</rss>";
        News news = News.create("Title", "Content", "https://url.com", "Test", null, "ko", "KR", null, LocalDateTime.now());

        when(restTemplate.getForObject(source.url(), byte[].class)).thenReturn(xml.getBytes(StandardCharsets.UTF_8));
        when(rssParser.parse(xml, source.name(), source.language(), source.region())).thenReturn(List.of(news));
        when(newsValidator.isDuplicate(news)).thenReturn(false);
        when(newsRepository.save(any(News.class))).thenReturn(news);

        List<News> result = newsGatheringService.gatherNewsFromRss(source);

        assertThat(result).hasSize(1);
        verify(newsRepository, times(1)).save(any(News.class));
    }

    @Test
    @DisplayName("UTF-8 RSS 응답은 깨지지 않은 문자열로 파서에 전달한다")
    void UTF8_RSS_응답_디코딩_테스트() {
        NewsSource source = new NewsSource("프레시안", "국내 뉴스", "https://example.com/rss", "ko", "KR");
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss><channel><item><title>이재명 정부</title></item></channel></rss>
                """;

        when(restTemplate.getForObject(source.url(), byte[].class)).thenReturn(xml.getBytes(StandardCharsets.UTF_8));
        when(rssParser.parse(xml, source.name(), source.language(), source.region())).thenReturn(List.of());

        List<News> result = newsGatheringService.gatherNewsFromRss(source);

        assertThat(result).isEmpty();
        verify(rssParser).parse(xml, source.name(), source.language(), source.region());
    }
}
