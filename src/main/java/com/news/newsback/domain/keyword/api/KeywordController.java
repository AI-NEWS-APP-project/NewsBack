package com.news.newsback.domain.keyword.api;

import com.news.newsback.domain.keyword.application.KeywordService;
import com.news.newsback.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@Tag(name = "Keyword API", description = "키워드 관련 API")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    @Operation(summary = "키워드 목록 조회", description = "사용자가 구독한 키워드 목록을 조회")
    public ApiResponse<List<String>> getKeywords(@RequestParam Long userId) {
        List<String> keywords = keywordService.retrieveKeywordsByUserId(userId);
        return ApiResponse.success(keywords);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 키워드 조회", description = "가장 많이 사용된 인기 키워드 목록을 조회")
    public ApiResponse<List<String>> getPopularKeywords() {
        List<String> popularKeywords = keywordService.retrievePopularKeywords();
        return ApiResponse.success(popularKeywords);
    }

    @PostMapping
    @Operation(summary = "키워드 추가", description = "새로운 키워드를 추가")
    public ApiResponse<String> addKeyword(@RequestParam Long userId, @RequestParam String keyword) {
        keywordService.registerKeyword(userId, keyword);
        return ApiResponse.success("키워드 추가 성공: " + keyword);
    }

    @PostMapping("/bulk")
    @Operation(summary = "키워드 일괄 등록", description = "다수의 키워드를 한 번에 등록")
    public ApiResponse<String> addKeywordsBulk(@RequestParam Long userId, @RequestBody List<String> keywords) {
        keywords.forEach(keyword -> keywordService.registerKeyword(userId, keyword));
        return ApiResponse.success("키워드 일괄 등록 성공");
    }

    @DeleteMapping
    @Operation(summary = "키워드 삭제", description = "기존 키워드를 삭제")
    public ApiResponse<String> deleteKeyword(@RequestParam Long userId, @RequestParam String keyword) {
        keywordService.deleteKeyword(userId, keyword);
        return ApiResponse.success("키워드 삭제 성공: " + keyword);
    }

    @DeleteMapping("/{keywordId}")
    @Operation(summary = "키워드 삭제", description = "구독 중인 키워드를 삭제")
    public ApiResponse<String> deleteKeywordById(@RequestParam Long userId, @PathVariable Long keywordId) {
        keywordService.deleteKeyword(userId, keywordId.toString());
        return ApiResponse.success("키워드 삭제 성공: " + keywordId);
    }
}
