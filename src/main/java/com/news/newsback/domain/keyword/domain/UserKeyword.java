package com.news.newsback.domain.keyword.domain;

import com.news.newsback.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_keyword",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_keyword_user_keyword",
                columnNames = {"user_id", "keyword_id"}
        ),
        indexes = {
                @Index(name = "idx_user_keyword_user_id", columnList = "user_id"),
                @Index(name = "idx_user_keyword_keyword_id", columnList = "keyword_id")
        }
)
@Getter
@NoArgsConstructor
public class UserKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Builder
    public UserKeyword(Long userId, Keyword keyword) {
        this.userId = userId;
        this.keyword = keyword;
    }
}
