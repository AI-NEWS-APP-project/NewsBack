package com.news.newsback.domain.news.service;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsValidator {

    private final NewsRepository newsRepository;

    public boolean isDuplicate(News news) {
        return newsRepository.existsByUrl(news.getUrl());
    }
}
