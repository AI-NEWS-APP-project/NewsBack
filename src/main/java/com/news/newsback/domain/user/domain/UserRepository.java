package com.news.newsback.domain.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmailIgnoreCase(String email);

    // 이메일 중복 확인
    boolean existsByEmailIgnoreCase(String email);
}
