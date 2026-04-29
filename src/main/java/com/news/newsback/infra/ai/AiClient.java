package com.news.newsback.infra.ai;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.news.model.ClusterNews;
import com.news.newsback.domain.news.model.News;

import java.util.List;

public interface AiClient {
    /**
     * 수집된 Row 뉴스들을 AI 서버로 전송 -> 클러스터링 ID 부여
     */
    void requestClusterId(List<News> newsList);

    /**
     * 특정 클러스터의 뉴스들을 요약 요청
     */
    void requestClusterNewsSummary(ClusterNews clusterNews, List<News> newsList);

    /**
     * 키워드별 관련 클러스터 뉴스들을 요약 요청
     */
    void requestKeywordNewsSummary(Keyword keyword, List<ClusterNews> clusterNewsList);

    /**
     * 일일 주요 뉴스 요약을 요청(보류)
     */
    void requestTodayNewsSummary(int topKImportant, int topKLatest, int timeWindowHours);
}
