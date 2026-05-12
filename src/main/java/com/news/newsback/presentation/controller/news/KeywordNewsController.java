package com.news.newsback.presentation.controller.news;

import com.news.newsback.application.news.KeywordNewsService;
import com.news.newsback.global.common.CommonReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "키워드 뉴스 요약 API")
public class KeywordNewsController {

    private final KeywordNewsService keywordNewsService;

    @Operation(
            summary = "키워드별 최신 뉴스 요약 조회",
            description = "최근 생성된 키워드 뉴스 요약을 최신순으로 최대 10개 조회합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "키워드별 최신 뉴스 요약 조회 성공")
    @GetMapping("/keyword-news/latest")
    public CommonReponse<List<KeywordNewsResponse.Summary>> getLatestKeywordNews() {
        return CommonReponse.success(keywordNewsService.getLatestKeywordNews());
    }

    @Operation(
            summary = "특정 키워드 뉴스 요약 히스토리 조회",
            description = "keywordId에 해당하는 키워드 뉴스 요약 이력을 최신순 페이지로 조회합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "키워드 뉴스 요약 히스토리 조회 성공")
    @GetMapping("/keyword-news")
    public CommonReponse<Page<KeywordNewsResponse.Summary>> getKeywordNewsHistory(
            @Parameter(description = "조회할 키워드 ID", example = "1")
            @RequestParam Long keywordId,
            @Parameter(description = "페이지 번호, 0부터 시작", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "6")
            @RequestParam(defaultValue = "6") int size) {
        return CommonReponse.success(keywordNewsService.getKeywordNewsHistory(keywordId, PageRequest.of(page, size)));
    }

    @Operation(
            summary = "키워드 뉴스 요약 상세 조회",
            description = "키워드 뉴스 요약 ID로 상세 요약과 관련 기사 링크를 조회합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "키워드 뉴스 요약 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "키워드 뉴스 요약을 찾을 수 없음")
    @GetMapping("/keyword-news/{id}")
    public CommonReponse<KeywordNewsResponse.Detail> getKeywordNewsDetail(
            @Parameter(description = "키워드 뉴스 요약 ID", example = "1")
            @PathVariable Long id) {
        return CommonReponse.success(keywordNewsService.getKeywordNewsDetail(id));
    }
}
