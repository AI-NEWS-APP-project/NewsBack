package com.news.newsback.domain.news.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_news_clusters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KeywordNewsCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_news_id", nullable = false)
    private KeywordNews keywordNews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private ClusterNews clusterNews;
}
