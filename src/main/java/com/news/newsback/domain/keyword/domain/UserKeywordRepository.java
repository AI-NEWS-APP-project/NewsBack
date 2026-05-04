package com.news.newsback.domain.keyword.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    int countByUserId(Long userId);

    boolean existsByUserIdAndKeywordId(Long userId, Long keywordId);

    @Modifying
    @Transactional
    void deleteByUserIdAndKeywordId(Long userId, Long keywordId);

    List<UserKeyword> findByUserId(Long userId);

    @Query("SELECT uk.keyword.name FROM UserKeyword uk GROUP BY uk.keyword.id ORDER BY COUNT(uk.keyword.id) DESC")
    List<String> findPopularKeywords();
}
