package com.news.newsback.domain.keyword.domain;

import com.news.newsback.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Keyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int usageCount = 0;

    // 키워드 엔티티: 키워드 이름 및 사용 횟수 관리
    @Builder
    public Keyword(String name) {
        this.name = name;
    }

    // 사용 횟수 증가 로직 추가 (-> 인기 키워드 조회)
    public void incrementUsageCount() {
        this.usageCount++;
    }

    public Long getId() {
        return id;
    }
}
