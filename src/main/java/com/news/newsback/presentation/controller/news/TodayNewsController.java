package com.news.newsback.presentation.controller.news;

import com.news.newsback.application.news.TodayNewsService;
import com.news.newsback.global.common.CommonReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "뉴스 요약 API")
public class TodayNewsController {

    private final TodayNewsService todayNewsService;

    @Operation(
            summary = "일일 통합 브리핑 조회",
            description = "가장 최근 생성된 일일 주요 뉴스 요약과 관련 원본 뉴스를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "일일 통합 브리핑 조회 성공")
    @ApiResponse(responseCode = "404", description = "일일 통합 브리핑을 찾을 수 없음")
    @GetMapping("/daily-briefings")
    public CommonReponse<TodayNewsResponse.Detail> getLatestDailyBriefing() {
        return CommonReponse.success(todayNewsService.getLatestTodayNewsSummary());
    }
}
