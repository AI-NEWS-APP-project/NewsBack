package com.news.newsback.domain.news.model;

import com.news.newsback.domain.keyword.domain.Keyword;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "keyword_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class KeywordNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "cluster_news_count")
    @Builder.Default
    private Integer clusterNewsCount = 1;

    @OneToMany(mappedBy = "keywordNews", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<KeywordNewsLink> links = new java.util.ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addLink(String url, String title) {
        this.links.add(KeywordNewsLink.create(this, url, title));
    }

    public void updateSummary(String summaryText, Integer clusterNewsCount) {
        this.summaryText = summaryText;
        this.clusterNewsCount = clusterNewsCount;
    }
}
