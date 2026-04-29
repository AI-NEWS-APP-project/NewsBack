package com.news.newsback.presentation.scheduler;

import com.news.newsback.application.news.NewsClusteringService;
import com.news.newsback.application.news.NewsGatheringService;
import com.news.newsback.application.news.NewsSummaryService;
import com.news.newsback.domain.news.model.NewsSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSummaryScheduler {

    private final NewsGatheringService newsGatheringService;
    private final NewsClusteringService newsClusteringService;
    private final NewsSummaryService newsSummaryService;

    private static final List<NewsSource> TARGET_SOURCES = List.of(
            new NewsSource("연합뉴스TV", "국내 뉴스", "https://www.yonhapnewstv.co.kr/browse/feed/", "ko", "KR"),
            new NewsSource("프레시안", "국내 뉴스", "http://www.pressian.com/api/v3/site/rss/news", "ko", "KR"),
            new NewsSource("BBC News", "세계 뉴스", "http://feeds.bbci.co.uk/news/world/rss.xml", "en", "GB")
    );

    // 30분 간격으로 실행
    @Scheduled(cron = "0 0/30 * * * *")
    public void runNewsGatheringAndClustering() {
        log.info("Starting scheduled news process...");

        // RSS 수집
        for (NewsSource source : TARGET_SOURCES) {
            try {
                newsGatheringService.gatherNewsFromRss(source);
            } catch (Exception e) {
                log.error("Failed to gather news from source: {}", source.name(), e);
            }
        }

        // 클러스터링 요청
        try {
            newsClusteringService.processUnclusteredNews();
        } catch (Exception e) {
            log.error("Failed to request clustering", e);
        }

        // 클러스터 요약
        try {
            newsSummaryService.requestClusterSummaries();
        } catch (Exception e) {
            log.error("Failed to request cluster summaries", e);
        }

        // 키워드 요약
        try {
            newsSummaryService.requestKeywordSummaries();
        } catch (Exception e) {
            log.error("Failed to request keyword summaries", e);
        }
    }

    // TODO: 일일 뉴스 요약 개발 보류. 기능 재개 시 스케줄 정책과 API 노출 여부를 함께 확정한다.
    // 일일 뉴스 요약 (매일 오전 8시, 오후 8시)
    // @Scheduled(cron = "0 0 8,20 * * *")
    // public void runTodayNewsSummary() {
    //     log.info("Starting today news summary process...");
    //     try {
    //         newsSummaryService.requestTodaySummary();
    //     } catch (Exception e) {
    //         log.error("Failed to request today news summary", e);
    //     }
    // }
}
