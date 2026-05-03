package com.news.newsback.application.news;

import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.repository.TodayNewsSummaryRepository;
import com.news.newsback.presentation.controller.news.TodayNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayNewsService {

    private final TodayNewsSummaryRepository todayNewsSummaryRepository;

    public TodayNewsResponse.Detail getLatestTodayNewsSummary() {
        return todayNewsSummaryRepository.findFirstByOrderByGeneratedAtDesc()
                .map(TodayNewsResponse.Detail::from)
                .orElseThrow(() -> new NewsException(NewsErrorCode.NEWS_NOT_FOUND));
    }
}
