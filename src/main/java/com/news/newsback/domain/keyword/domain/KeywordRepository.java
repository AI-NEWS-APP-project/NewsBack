package com.news.newsback.domain.keyword.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByName(String name);

    @Query("SELECT k.name FROM Keyword k ORDER BY k.usageCount DESC")
    List<String> findPopularKeywords();
}
