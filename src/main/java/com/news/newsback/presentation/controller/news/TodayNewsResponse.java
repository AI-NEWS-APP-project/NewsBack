package com.news.newsback.presentation.controller.news;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.model.TodayNewsSummary;
import com.news.newsback.domain.news.model.TodayNewsSummaryNews;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class TodayNewsResponse {

    @Getter
    @Builder
    @Schema(description = "일일 뉴스 요약 응답")
    public static class Summary {

        @Schema(description = "일일 뉴스 요약 ID", example = "1")
        private Long id;

        @Schema(description = "일일 뉴스 요약 제목", example = "최근 주요 뉴스 종합")
        private String title;

        @Schema(description = "일일 주요 뉴스 요약", example = "오늘은 반도체 투자와 외교 이슈가 주요 뉴스로 다뤄졌습니다.")
        private String summary;

        @Schema(description = "요약에 반영된 뉴스 개수", example = "20")
        private Integer newsCount;

        @Schema(description = "요약 생성 시각", example = "2026-05-01T20:00:00")
        private LocalDateTime generatedAt;

        public static Summary from(TodayNewsSummary summary) {
            return Summary.builder()
                    .id(summary.getId())
                    .title(summary.getTitle())
                    .summary(summary.getSummary())
                    .newsCount(summary.getNewsCount())
                    .generatedAt(summary.getGeneratedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "일일 뉴스 요약 상세 응답")
    public static class Detail {

        @Schema(description = "일일 뉴스 요약 ID", example = "1")
        private Long id;

        @Schema(description = "일일 뉴스 요약 제목", example = "최근 주요 뉴스 종합")
        private String title;

        @Schema(description = "일일 주요 뉴스 요약", example = "오늘은 반도체 투자와 외교 이슈가 주요 뉴스로 다뤄졌습니다.")
        private String summary;

        @Schema(description = "요약에 반영된 뉴스 개수", example = "20")
        private Integer newsCount;

        @Schema(description = "요약 생성 시각", example = "2026-05-01T20:00:00")
        private LocalDateTime generatedAt;

        @Schema(description = "요약에 포함된 원본 뉴스 목록")
        private List<NewsItem> news;

        public static Detail from(TodayNewsSummary summary) {
            return Detail.builder()
                    .id(summary.getId())
                    .title(summary.getTitle())
                    .summary(summary.getSummary())
                    .newsCount(summary.getNewsCount())
                    .generatedAt(summary.getGeneratedAt())
                    .news(summary.getSummaryNews().stream()
                            .map(TodayNewsSummaryNews::getNews)
                            .map(NewsItem::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "일일 뉴스 요약 원본 뉴스")
    public static class NewsItem {

        @Schema(description = "뉴스 ID", example = "74a6216d-21c1-4c61-9f43-88757bfc9e3a")
        private String id;

        @Schema(description = "뉴스 제목", example = "삼성전자, 반도체 투자 확대")
        private String title;

        @Schema(description = "뉴스 URL", example = "https://news.example.com/1")
        private String url;

        @Schema(description = "언론사", example = "연합뉴스TV")
        private String source;

        @Schema(description = "뉴스 발행 시각", example = "2026-05-01T10:00:00")
        private LocalDateTime publishedAt;

        public static NewsItem from(News news) {
            return NewsItem.builder()
                    .id(news.getId())
                    .title(news.getTitle())
                    .url(news.getUrl())
                    .source(news.getSource())
                    .publishedAt(news.getPublishedAt())
                    .build();
        }
    }
}
