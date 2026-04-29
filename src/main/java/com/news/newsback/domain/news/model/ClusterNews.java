package com.news.newsback.domain.news.model;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cluster_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClusterNews extends BaseTimeEntity {

    @Id
    @Column(length = 36)
    private String id; // UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    @Column(length = 500)
    private String title;

    @Column(name = "representative_summary", columnDefinition = "TEXT")
    private String representativeSummary;

    @Column(name = "news_count")
    @Builder.Default
    private Integer newsCount = 1;

    @Column(name = "last_summarized_count")
    @Builder.Default
    private Integer lastSummarizedCount = 1;

    public void updateSummary(String title, String summary) {
        this.title = title;
        this.representativeSummary = summary;
        this.lastSummarizedCount = this.newsCount;
    }

    public void incrementNewsCount() {
        this.newsCount++;
    }

    public boolean requiresSummary(int newsIncrementThreshold) {
        return representativeSummary == null
                || newsCount - lastSummarizedCount >= newsIncrementThreshold;
    }
}
