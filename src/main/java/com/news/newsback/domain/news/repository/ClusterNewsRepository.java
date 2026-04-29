package com.news.newsback.domain.news.repository;

import com.news.newsback.domain.news.model.ClusterNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClusterNewsRepository extends JpaRepository<ClusterNews, String> {

    @Query("""
            select c
            from ClusterNews c
            where c.representativeSummary is null
               or c.newsCount - c.lastSummarizedCount >= :newsIncrementThreshold
            """)
    List<ClusterNews> findAllRequiringSummary(@Param("newsIncrementThreshold") int newsIncrementThreshold);

    List<ClusterNews> findAllByRepresentativeSummaryIsNotNull();
}
