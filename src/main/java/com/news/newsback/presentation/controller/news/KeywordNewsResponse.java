package com.news.newsback.presentation.controller.news;

import com.news.newsback.domain.news.model.KeywordNews;
import com.news.newsback.domain.news.model.KeywordNewsLink;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class KeywordNewsResponse {

    @Getter
    @Builder
    @Schema(description = "키워드 뉴스 요약 응답")
    public static class Summary {

        @Schema(description = "키워드 뉴스 요약 ID", example = "1")
        private Long id;

        @Schema(description = "키워드 ID", example = "10")
        private Long keywordId;

        @Schema(description = "키워드 이름", example = "경제")
        private String keywordName;

        @Schema(description = "키워드 기반 뉴스 요약", example = "오늘 경제 뉴스는 환율과 금리 이슈가 중심입니다.")
        private String summaryText;

        @Schema(description = "요약에 반영된 클러스터 뉴스 개수", example = "3")
        private Integer clusterNewsCount;

        @Schema(description = "요약 생성 시각", example = "2026-04-28T10:00:00")
        private LocalDateTime createdAt;

        public static Summary from(KeywordNews keywordNews) {
            return Summary.builder()
                    .id(keywordNews.getId())
                    .keywordId(keywordNews.getKeyword().getId())
                    .keywordName(keywordNews.getKeyword().getName())
                    .summaryText(keywordNews.getSummaryText())
                    .clusterNewsCount(keywordNews.getClusterNewsCount())
                    .createdAt(keywordNews.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "키워드 뉴스 요약 상세 응답")
    public static class Detail {

        @Schema(description = "키워드 뉴스 요약 ID", example = "1")
        private Long id;

        @Schema(description = "키워드 ID", example = "10")
        private Long keywordId;

        @Schema(description = "키워드 이름", example = "경제")
        private String keywordName;

        @Schema(description = "키워드 기반 뉴스 요약", example = "오늘 경제 뉴스는 환율과 금리 이슈가 중심입니다.")
        private String summaryText;

        @Schema(description = "요약에 반영된 클러스터 뉴스 개수", example = "3")
        private Integer clusterNewsCount;

        @Schema(description = "요약 생성 시각", example = "2026-04-28T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "요약에 포함된 관련 기사 링크 목록")
        private List<Link> links;

        public static Detail from(KeywordNews keywordNews) {
            return Detail.builder()
                    .id(keywordNews.getId())
                    .keywordId(keywordNews.getKeyword().getId())
                    .keywordName(keywordNews.getKeyword().getName())
                    .summaryText(keywordNews.getSummaryText())
                    .clusterNewsCount(keywordNews.getClusterNewsCount())
                    .createdAt(keywordNews.getCreatedAt())
                    .links(keywordNews.getLinks().stream()
                            .map(Link::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "키워드 뉴스 관련 기사 링크")
    public static class Link {

        @Schema(description = "기사 URL", example = "https://news.example.com/1")
        private String url;

        @Schema(description = "기사 제목", example = "기준금리 동결, 시장 반응은")
        private String title;

        public static Link from(KeywordNewsLink link) {
            return Link.builder()
                    .url(link.getUrl())
                    .title(link.getTitle())
                    .build();
        }
    }
}
