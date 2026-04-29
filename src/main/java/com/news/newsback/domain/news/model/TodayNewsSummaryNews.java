package com.news.newsback.domain.news.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "today_news_summary_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TodayNewsSummaryNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_news_summary_id", nullable = false)
    private TodayNewsSummary todayNewsSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    public static TodayNewsSummaryNews create(TodayNewsSummary todayNewsSummary, News news) {
        return TodayNewsSummaryNews.builder()
                .todayNewsSummary(todayNewsSummary)
                .news(news)
                .build();
    }
}
