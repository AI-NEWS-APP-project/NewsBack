package com.news.newsback.domain.news.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_news_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KeywordNewsLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_news_id", nullable = false)
    private KeywordNews keywordNews;

    @Column(nullable = false, length = 768)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    public static KeywordNewsLink create(KeywordNews keywordNews, String url, String title) {
        return KeywordNewsLink.builder()
                .keywordNews(keywordNews)
                .url(url)
                .title(title)
                .build();
    }
}
