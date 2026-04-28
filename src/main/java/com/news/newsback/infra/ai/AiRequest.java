package com.news.newsback.infra.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AiRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterIdRequest {
        @JsonProperty("request_id")
        private String requestId;
        @JsonProperty("callback_url")
        private String callbackUrl;
        private List<RowNews> news;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowNews {
        @JsonProperty("rownews_id")
        private String rownewsId;
        private String title;
        private String content;
        @JsonProperty("created_at")
        private String createdAt;
        private String url; // Optional, preserved for backward compatibility if needed
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterNewsSummaryRequest {
        @JsonProperty("request_id")
        private String requestId;
        @JsonProperty("callback_url")
        private String callbackUrl;
        @JsonProperty("cluster_id")
        private String clusterId;
        private List<RowNews> news;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordNewsRequest {
        @JsonProperty("request_id")
        private String requestId;
        @JsonProperty("callback_url")
        private String callbackUrl;
        @JsonProperty("keyword_id")
        private Long keywordId;
        private String keyword;
        @JsonProperty("cluster_news")
        private List<ClusterNewsItem> clusterNews;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterNewsItem {
        @JsonProperty("cluster_id")
        private String clusterId;
        private String title;
        private String summary;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayNewsRequest {
        @JsonProperty("request_id")
        private String requestId;
        @JsonProperty("callback_url")
        private String callbackUrl;
        @JsonProperty("top_k_important")
        private int topKImportant;
        @JsonProperty("top_k_latest")
        private int topKLatest;
        @JsonProperty("time_window_hours")
        private int timeWindowHours;
    }
}
