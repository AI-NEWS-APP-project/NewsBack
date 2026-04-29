package com.news.newsback.infra.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AiResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterIdResponse {
        @JsonProperty("request_id")
        private String requestId;
        private String status;
        private List<ClusterIdResult> results;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterIdResult {
        @JsonProperty("rownews_id")
        private String rownewsId;
        @JsonProperty("cluster_id")
        private String clusterId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterNewsSummaryResponse {
        @JsonProperty("request_id")
        private String requestId;
        private String status;
        @JsonProperty("cluster_id")
        private String clusterId;
        private String title;
        private String summary;
        @JsonProperty("source_news_ids")
        private List<String> sourceNewsIds;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordNewsResponse {
        @JsonProperty("request_id")
        private String requestId;
        private String status;
        @JsonProperty("keyword_id")
        private Long keywordId;
        private String keyword;
        private String title;
        private String summary;
        @JsonProperty("related_cluster_ids")
        private List<String> relatedClusterIds;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayNewsResponse {
        @JsonProperty("request_id")
        private String requestId;
        private String status;
        private String title;
        private String summary;
        @JsonProperty("news_ids")
        private List<String> newsIds;
        @JsonProperty("news_count")
        private Integer newsCount;
        @JsonProperty("generated_at")
        private String generatedAt;
    }
}
