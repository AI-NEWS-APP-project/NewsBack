package com.news.newsback.domain.news.repository;

import com.news.newsback.domain.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, String> {
    Optional<News> findByUrl(String url);
    boolean existsByUrl(String url);
    List<News> findAllByClusterIdIsNull();
    List<News> findAllByClusterIdOrderByPublishedAtDesc(String clusterId);
}
