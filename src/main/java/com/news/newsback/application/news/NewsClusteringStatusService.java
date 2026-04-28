package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsClusteringStatusService {

    private final NewsRepository newsRepository;

    @Transactional
    public List<News> markUnclusteredNewsAsProcessing() {
        List<News> targets = newsRepository.findAllByClusterIdIsNull();
        targets.forEach(News::markAsProcessing);
        return targets;
    }

    @Transactional
    public void markNewsAsError(List<News> newsList) {
        List<String> newsIds = newsList.stream()
                .map(News::getId)
                .toList();

        newsRepository.findAllById(newsIds)
                .forEach(News::markAsError);
    }
}
