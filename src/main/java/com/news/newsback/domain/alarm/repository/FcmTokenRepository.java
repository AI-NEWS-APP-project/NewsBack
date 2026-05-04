package com.news.newsback.domain.alarm.repository;

import com.news.newsback.domain.alarm.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByUserIdAndToken(Long userId, String token);

    List<FcmToken> findByTokenIn(Collection<String> tokens);

    @Query("select t from FcmToken t where t.user.id in :userIds and t.enabled = true and t.user.globalPushEnabled = true")
    List<FcmToken> findEnabledTokensByUserIdsWithPushEnabled(Collection<Long> userIds);

    @Query("select t from FcmToken t where t.enabled = true and t.user.globalPushEnabled = true")
    List<FcmToken> findAllEnabledTokensWithPushEnabled();
}
