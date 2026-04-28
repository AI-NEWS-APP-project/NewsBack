package com.news.newsback.domain.news.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "today_news_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TodayNewsSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "news_count")
    @Builder.Default
    private Integer newsCount = 0;

    @OneToMany(mappedBy = "todayNewsSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TodayNewsSummaryNews> summaryNews = new ArrayList<>();

    @CreatedDate
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    public void addNews(News news) {
        this.summaryNews.add(TodayNewsSummaryNews.create(this, news));
        this.newsCount = this.summaryNews.size();
    }
}
