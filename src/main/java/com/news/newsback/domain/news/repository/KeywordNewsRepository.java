package com.news.newsback.domain.news.repository;

import com.news.newsback.domain.news.model.KeywordNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordNewsRepository extends JpaRepository<KeywordNews, Long> {

    List<KeywordNews> findTop10ByOrderByCreatedAtDesc();

    Page<KeywordNews> findByKeywordIdOrderByCreatedAtDesc(Long keywordId, Pageable pageable);
}
