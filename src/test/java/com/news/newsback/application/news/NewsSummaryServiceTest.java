package com.news.newsback.application.news;

import com.news.newsback.application.notification.PushNotificationService;
import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.model.ClusterNews;
import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.news.repository.ClusterNewsRepository;
import com.news.newsback.domain.news.repository.KeywordNewsRepository;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.domain.news.repository.TodayNewsSummaryRepository;
import com.news.newsback.infra.ai.AiClient;
import com.news.newsback.infra.ai.AiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsSummaryService 단위 테스트")
class NewsSummaryServiceTest {

    @InjectMocks
    private NewsSummaryService newsSummaryService;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private ClusterNewsRepository clusterNewsRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private KeywordNewsRepository keywordNewsRepository;

    @Mock
    private TodayNewsSummaryRepository todayNewsSummaryRepository;

    @Mock
    private AiClient aiClient;

    @Mock
    private PushNotificationService pushNotificationService;

    @Test
    @DisplayName("요약이 필요하다고 조회된 클러스터 뉴스만 클러스터 요약 요청으로 전송한다")
    void 요약_필요_클러스터만_클러스터_요약_요청() {
        ClusterNews clusterNews = clusterNews("cluster-1", null, null);
        com.news.newsback.domain.news.model.News news = com.news.newsback.domain.news.model.News.create(
                "제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, java.time.LocalDateTime.now());

        when(clusterNewsRepository.findAllRequiringSummary(3)).thenReturn(List.of(clusterNews));
        when(newsRepository.findTop5ByClusterIdOrderByPublishedAtDesc("cluster-1")).thenReturn(List.of(news));

        newsSummaryService.requestClusterSummaries();

        verify(aiClient).requestClusterNewsSummary(clusterNews, List.of(news));
    }

    @Test
    @DisplayName("클러스터 요약 요청에는 최신 row news 최대 5개만 전송한다")
    void 클러스터_요약_최신_5개만_요청() {
        ClusterNews clusterNews = clusterNews("cluster-1", null, null);
        List<com.news.newsback.domain.news.model.News> latestFiveNews = java.util.stream.IntStream.rangeClosed(1, 5)
                .mapToObj(index -> com.news.newsback.domain.news.model.News.create(
                        "제목" + index,
                        "본문" + index,
                        "https://news.example.com/" + index,
                        "언론사",
                        null,
                        "ko",
                        "KR",
                        null,
                        java.time.LocalDateTime.now().minusMinutes(index)
                ))
                .toList();

        when(clusterNewsRepository.findAllRequiringSummary(3)).thenReturn(List.of(clusterNews));
        when(newsRepository.findTop5ByClusterIdOrderByPublishedAtDesc("cluster-1")).thenReturn(latestFiveNews);

        newsSummaryService.requestClusterSummaries();

        verify(aiClient).requestClusterNewsSummary(clusterNews, latestFiveNews);
    }

    @Test
    @DisplayName("클러스터 요약 callback 성공 시 대표 요약과 마지막 요약 뉴스 개수를 갱신한다")
    void 클러스터_요약_callback_성공_시_요약_상태_갱신() {
        ClusterNews clusterNews = ClusterNews.builder()
                .id("cluster-1")
                .newsCount(6)
                .lastSummarizedCount(1)
                .build();
        AiResponse.ClusterNewsSummaryResponse response = new AiResponse.ClusterNewsSummaryResponse(
                "request-id", "success", "cluster-1", "요약 제목", "대표 요약", List.of("news-1"), "2026-04-28T10:00:00");

        when(clusterNewsRepository.findById("cluster-1")).thenReturn(Optional.of(clusterNews));

        newsSummaryService.updateClusterNewsSummary(response);

        assertThat(clusterNews.getTitle()).isEqualTo("요약 제목");
        assertThat(clusterNews.getRepresentativeSummary()).isEqualTo("대표 요약");
        assertThat(clusterNews.getLastSummarizedCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("키워드 뉴스 callback의 키워드가 없으면 NewsException을 던진다")
    void 키워드_뉴스_callback_키워드_없음() {
        AiResponse.KeywordNewsResponse response = new AiResponse.KeywordNewsResponse(
                "request-id", "success", 99L, "삼성전자", "제목", "요약", List.of("cluster-1"), "2026-04-28T10:00:00");

        when(keywordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsSummaryService.saveKeywordNews(response))
                .isInstanceOf(NewsException.class)
                .extracting("errorCode")
                .isEqualTo(NewsErrorCode.KEYWORD_NOT_FOUND);
    }

    @Test
    @DisplayName("키워드 뉴스 callback이 success가 아니면 저장하지 않는다")
    void 키워드_뉴스_callback_비성공_저장하지_않음() {
        AiResponse.KeywordNewsResponse response = new AiResponse.KeywordNewsResponse(
                "request-id", "fail", 1L, "삼성전자", null, null, List.of(), "2026-04-28T10:00:00");

        newsSummaryService.saveKeywordNews(response);

        verifyNoInteractions(keywordRepository, newsRepository, keywordNewsRepository);
    }

    @Test
    @DisplayName("키워드 뉴스 callback 성공 시 저장 후 푸시 알림을 요청한다")
    void 키워드_뉴스_callback_성공_푸시_알림_요청() {
        Keyword keyword = keyword(1L, "AI");
        AiResponse.KeywordNewsResponse response = new AiResponse.KeywordNewsResponse(
                "request-id", "success", 1L, "AI", "AI 키워드 뉴스", "AI 요약", List.of("cluster-1"), "2026-04-28T10:00:00");
        com.news.newsback.domain.news.model.News news = com.news.newsback.domain.news.model.News.create(
                "기사 제목", "기사 본문", "https://news.example.com/1", "언론사", null, "ko", "KR", null, java.time.LocalDateTime.now());

        when(keywordRepository.findById(1L)).thenReturn(Optional.of(keyword));
        when(newsRepository.findAllByClusterIdOrderByPublishedAtDesc("cluster-1")).thenReturn(List.of(news));
        when(keywordNewsRepository.save(org.mockito.Mockito.any(KeywordNews.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        newsSummaryService.saveKeywordNews(response);

        ArgumentCaptor<KeywordNews> captor = ArgumentCaptor.forClass(KeywordNews.class);
        verify(keywordNewsRepository).save(captor.capture());
        verify(pushNotificationService).sendKeywordNews(captor.getValue());
    }

    @Test
    @DisplayName("일일 뉴스 요약 요청은 문서 스펙의 기본 파라미터로 전송한다")
    void 일일_뉴스_요약_요청() {
        newsSummaryService.requestTodaySummary();

        verify(aiClient).requestTodayNewsSummary(10, 10, 24);
    }

    @Test
    @DisplayName("일일 뉴스 요약 callback 성공 시 요약과 관련 뉴스를 저장한다")
    void 일일_뉴스_요약_callback_성공_저장() {
        com.news.newsback.domain.news.model.News news = com.news.newsback.domain.news.model.News.create(
                "제목", "본문", "https://news.example.com", "언론사", null, "ko", "KR", null, java.time.LocalDateTime.now());
        AiResponse.TodayNewsResponse response = new AiResponse.TodayNewsResponse(
                "request-id", "success", "최근 주요 뉴스 종합", "일일 요약", List.of(news.getId()), 1, "2026-04-28T20:00:00");

        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        newsSummaryService.saveTodayNewsSummary(response);

        ArgumentCaptor<com.news.newsback.domain.news.model.TodayNewsSummary> captor =
                ArgumentCaptor.forClass(com.news.newsback.domain.news.model.TodayNewsSummary.class);
        verify(todayNewsSummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("최근 주요 뉴스 종합");
        assertThat(captor.getValue().getSummary()).isEqualTo("일일 요약");
        assertThat(captor.getValue().getNewsCount()).isEqualTo(1);
        assertThat(captor.getValue().getSummaryNews()).hasSize(1);
    }

    @Test
    @DisplayName("일일 뉴스 요약 callback이 success가 아니면 저장하지 않는다")
    void 일일_뉴스_요약_callback_비성공_저장하지_않음() {
        AiResponse.TodayNewsResponse response = new AiResponse.TodayNewsResponse(
                "request-id", "fail", null, null, List.of(), 0, "2026-04-28T20:00:00");

        newsSummaryService.saveTodayNewsSummary(response);

        verify(todayNewsSummaryRepository, never()).save(org.mockito.Mockito.any());
    }

    @Test
    @DisplayName("키워드가 제목 또는 대표 요약에 포함된 클러스터 뉴스만 키워드 요약 요청으로 전송한다")
    void 키워드_매칭_클러스터만_키워드_요약_요청() {
        Keyword keyword = keyword(1L, "삼성전자");
        ClusterNews matchedByTitle = clusterNews("cluster-1", "삼성전자 반도체 투자 확대", "반도체 투자가 확대되고 있다.");
        ClusterNews matchedBySummary = clusterNews("cluster-2", "반도체 투자 확대", "삼성전자가 AI 투자를 확대했다.");
        ClusterNews notMatched = clusterNews("cluster-3", "현대차 신차 출시", "전기차 판매가 늘었다.");

        when(keywordRepository.findAll()).thenReturn(List.of(keyword));
        when(clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull())
                .thenReturn(List.of(matchedByTitle, matchedBySummary, notMatched));

        newsSummaryService.requestKeywordSummaries();

        ArgumentCaptor<List<ClusterNews>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiClient).requestKeywordNewsSummary(org.mockito.Mockito.eq(keyword), captor.capture());
        assertThat(captor.getValue())
                .extracting(ClusterNews::getId)
                .containsExactly("cluster-1", "cluster-2");
    }

    @Test
    @DisplayName("키워드 요약 요청에는 매칭된 클러스터 뉴스 최대 10개만 전송한다")
    void 키워드_요약_매칭_클러스터_최대_10개만_요청() {
        Keyword keyword = keyword(1L, "AI");
        List<ClusterNews> matchedClusters = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(index -> clusterNews("cluster-" + index, "AI 관련 뉴스 " + index, "AI 요약 " + index))
                .toList();

        when(keywordRepository.findAll()).thenReturn(List.of(keyword));
        when(clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull()).thenReturn(matchedClusters);

        newsSummaryService.requestKeywordSummaries();

        ArgumentCaptor<List<ClusterNews>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiClient).requestKeywordNewsSummary(org.mockito.Mockito.eq(keyword), captor.capture());
        assertThat(captor.getValue())
                .hasSize(10)
                .extracting(ClusterNews::getId)
                .containsExactly(
                        "cluster-1",
                        "cluster-2",
                        "cluster-3",
                        "cluster-4",
                        "cluster-5",
                        "cluster-6",
                        "cluster-7",
                        "cluster-8",
                        "cluster-9",
                        "cluster-10"
                );
    }

    @Test
    @DisplayName("등록된 키워드가 없으면 클러스터 뉴스를 조회하지 않는다")
    void 등록된_키워드가_없으면_클러스터_조회하지_않음() {
        when(keywordRepository.findAll()).thenReturn(List.of());

        newsSummaryService.requestKeywordSummaries();

        verifyNoInteractions(clusterNewsRepository);
        verify(aiClient, never()).requestKeywordNewsSummary(org.mockito.Mockito.any(), org.mockito.Mockito.anyList());
    }

    @Test
    @DisplayName("요약 완료된 클러스터 뉴스가 없으면 키워드 요약 요청을 보내지 않는다")
    void 요약_완료_클러스터가_없으면_요청하지_않음() {
        when(keywordRepository.findAll()).thenReturn(List.of(keyword(1L, "삼성전자")));
        when(clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull()).thenReturn(List.of());

        newsSummaryService.requestKeywordSummaries();

        verify(aiClient, never()).requestKeywordNewsSummary(org.mockito.Mockito.any(), org.mockito.Mockito.anyList());
    }

    @Test
    @DisplayName("빈 키워드는 키워드 요약 요청에서 제외한다")
    void 빈_키워드는_키워드_요약_요청에서_제외() {
        Keyword blankKeyword = keyword(1L, "   ");
        ClusterNews clusterNews = clusterNews("cluster-1", "삼성전자 반도체 투자 확대", "반도체 투자가 확대되고 있다.");

        when(keywordRepository.findAll()).thenReturn(List.of(blankKeyword));
        when(clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull()).thenReturn(List.of(clusterNews));

        newsSummaryService.requestKeywordSummaries();

        verify(aiClient, never()).requestKeywordNewsSummary(org.mockito.Mockito.any(), org.mockito.Mockito.anyList());
    }

    @Test
    @DisplayName("키워드와 매칭되는 클러스터 뉴스가 없으면 키워드 요약 요청을 보내지 않는다")
    void 키워드_매칭_클러스터가_없으면_요청하지_않음() {
        Keyword keyword = keyword(1L, "삼성전자");
        ClusterNews notMatched = clusterNews("cluster-1", "현대차 신차 출시", "전기차 판매가 늘었다.");

        when(keywordRepository.findAll()).thenReturn(List.of(keyword));
        when(clusterNewsRepository.findAllByRepresentativeSummaryIsNotNull())
                .thenReturn(List.of(notMatched));

        newsSummaryService.requestKeywordSummaries();

        verify(aiClient, never()).requestKeywordNewsSummary(org.mockito.Mockito.any(), org.mockito.Mockito.anyList());
    }

    private Keyword keyword(Long id, String name) {
        Keyword keyword = Keyword.builder()
                .name(name)
                .build();
        keyword.setId(id);
        return keyword;
    }

    private ClusterNews clusterNews(String id, String title, String summary) {
        return ClusterNews.builder()
                .id(id)
                .title(title)
                .representativeSummary(summary)
                .build();
    }
}
