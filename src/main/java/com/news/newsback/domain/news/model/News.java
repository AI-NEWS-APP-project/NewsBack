package com.news.newsback.domain.news.model;

import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class News extends BaseTimeEntity {

    @Id
    @Column(length = 64)
    private String id; // UUID

    @Column(name = "cluster_id", length = 36)
    private String clusterId; // UUID

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 255)
    @Builder.Default
    private String author = "Unknown";

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, unique = true, length = 768)
    private String url;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    public enum ProcessingStatus {
        READY, PROCESSING, COMPLETED, ERROR
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.READY;

    public static News create(String title, String content, String url, String source, 
                              String author, String language, String region, 
                              String thumbnailUrl, LocalDateTime publishedAt) {
        validateRequiredFields(title, content, url);
        
        return News.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .content(content)
                .url(url)
                .source(source)
                .author(author == null ? "Unknown" : author)
                .language(language)
                .region(region)
                .thumbnailUrl(thumbnailUrl)
                .publishedAt(publishedAt)
                .build();
    }

    private static void validateRequiredFields(String title, String content, String url) {
        if (title == null || title.isBlank() || content == null || content.isBlank() || url == null || url.isBlank()) {
            throw new NewsException(NewsErrorCode.NEWS_REQUIRED_FIELD_MISSING);
        }
    }

    public void updateClusterId(String clusterId) {
        this.clusterId = clusterId;
        this.processingStatus = ProcessingStatus.COMPLETED;
    }

    public void markAsProcessing() {
        this.processingStatus = ProcessingStatus.PROCESSING;
    }

    public void markAsError() {
        this.processingStatus = ProcessingStatus.ERROR;
    }

    public void updateProcessingStatus(ProcessingStatus status) {
        this.processingStatus = status;
    }
}
