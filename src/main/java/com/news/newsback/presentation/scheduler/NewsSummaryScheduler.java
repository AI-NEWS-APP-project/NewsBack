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
            new NewsSource("프레시안", "국내 뉴스", "https://www.pressian.com/api/v3/site/rss/news", "ko", "KR"),
            new NewsSource("동아일보", "국내 뉴스", "https://rss.donga.com/total.xml", "ko", "KR"),
            new NewsSource("한겨레", "국내 뉴스", "https://www.hani.co.kr/rss", "ko", "KR"),
            new NewsSource("한국경제", "국내 뉴스", "https://www.hankyung.com/feed/all-news", "ko", "KR"),
            new NewsSource("SBS 뉴스", "국내 뉴스", "https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=01&plink=RSSREADER", "ko", "KR"),
            new NewsSource("매일경제", "국내 뉴스", "https://www.mk.co.kr/rss/30000001", "ko", "KR"),
            new NewsSource("BBC News", "세계 뉴스", "https://feeds.bbci.co.uk/news/world/rss.xml", "en", "GB")
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

        // 클러스터 요약 -> 메모리
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

    // 일일 뉴스 요약 (매일 오전 8시, 오후 8시)
    //FIXME: 테스트용 임시 cron(2분 간격 요청)
    @Scheduled(cron = "0 0 8,20 * * *")
//    @Scheduled(cron = "0 0/2 * * * *")
    public void runTodayNewsSummary() {
        log.info("Starting today news summary process...");
        try {
            newsSummaryService.requestTodaySummary();
        } catch (Exception e) {
            log.error("Failed to request today news summary", e);
        }
    }
}
