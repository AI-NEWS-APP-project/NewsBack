package com.news.newsback.domain.news.repository;

import com.news.newsback.domain.news.model.TodayNewsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayNewsSummaryRepository extends JpaRepository<TodayNewsSummary, Long> {
}
