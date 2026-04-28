package com.news.newsback.unit.domain.news;

import com.news.newsback.domain.news.model.ClusterNews;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClusterNews 단위 테스트")
class ClusterNewsTest {

    @Test
    @DisplayName("대표 요약이 없으면 요약이 필요하다")
    void 대표_요약이_없으면_요약_필요() {
        ClusterNews clusterNews = ClusterNews.builder()
                .id("cluster-1")
                .representativeSummary(null)
                .newsCount(1)
                .lastSummarizedCount(1)
                .build();

        assertThat(clusterNews.requiresSummary(5)).isTrue();
    }

    @Test
    @DisplayName("마지막 요약 이후 뉴스가 기준 개수 이상 추가되면 요약이 필요하다")
    void 뉴스가_기준_개수_이상_추가되면_요약_필요() {
        ClusterNews clusterNews = ClusterNews.builder()
                .id("cluster-1")
                .representativeSummary("기존 요약")
                .newsCount(6)
                .lastSummarizedCount(1)
                .build();

        assertThat(clusterNews.requiresSummary(5)).isTrue();
    }

    @Test
    @DisplayName("요약 갱신 시 마지막 요약 뉴스 개수를 현재 뉴스 개수로 맞춘다")
    void 요약_갱신_시_마지막_요약_뉴스_개수_갱신() {
        ClusterNews clusterNews = ClusterNews.builder()
                .id("cluster-1")
                .newsCount(6)
                .lastSummarizedCount(1)
                .build();

        clusterNews.updateSummary("제목", "대표 요약");

        assertThat(clusterNews.getTitle()).isEqualTo("제목");
        assertThat(clusterNews.getRepresentativeSummary()).isEqualTo("대표 요약");
        assertThat(clusterNews.getLastSummarizedCount()).isEqualTo(6);
    }
}
