package com.news.newsback.application.news;

import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.repository.KeywordNewsRepository;
import com.news.newsback.presentation.controller.news.KeywordNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordNewsService {

    private final KeywordNewsRepository keywordNewsRepository;
    private final KeywordRepository keywordRepository;

    public List<KeywordNewsResponse.Summary> getLatestKeywordNews() {
        return keywordNewsRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(KeywordNewsResponse.Summary::from)
                .toList();
    }

    public Page<KeywordNewsResponse.Summary> getKeywordNewsHistory(Long keywordId, Pageable pageable) {
        if (!keywordRepository.existsById(keywordId)) {
            throw new NewsException(NewsErrorCode.NEWS_NOT_FOUND);
        }

        return keywordNewsRepository.findByKeywordIdOrderByCreatedAtDesc(keywordId, pageable)
                .map(KeywordNewsResponse.Summary::from);
    }

    public KeywordNewsResponse.Detail getKeywordNewsDetail(Long id) {
        return keywordNewsRepository.findById(id)
                .map(KeywordNewsResponse.Detail::from)
                .orElseThrow(() -> new NewsException(NewsErrorCode.NEWS_NOT_FOUND));
    }
}
