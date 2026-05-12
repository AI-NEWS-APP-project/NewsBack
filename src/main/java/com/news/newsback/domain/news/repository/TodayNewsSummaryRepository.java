package com.news.newsback.domain.news.repository;

import com.news.newsback.domain.news.model.TodayNewsSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodayNewsSummaryRepository extends JpaRepository<TodayNewsSummary, Long> {

    @EntityGraph(attributePaths = {"summaryNews", "summaryNews.news"})
    Optional<TodayNewsSummary> findFirstByOrderByGeneratedAtDesc();

    @EntityGraph(attributePaths = {"summaryNews", "summaryNews.news"})
    Optional<TodayNewsSummary> findWithSummaryNewsById(Long id);
}
