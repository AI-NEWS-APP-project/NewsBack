package com.news.newsback.domain.keyword.api;

import com.news.newsback.domain.keyword.application.KeywordService;
import com.news.newsback.domain.keyword.dto.KeywordDto;
import com.news.newsback.global.common.CommonReponse;
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
    @Operation(summary = "키워드 목록 조회", description = "사용자가 구독한 키워드 목록을 조회합니다.")
    public CommonReponse<List<KeywordDto>> getKeywords(@RequestParam Long userId) {
        List<KeywordDto> keywords = keywordService.retrieveKeywordsByUserId(userId);
        return CommonReponse.success(keywords);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 키워드 조회", description = "가장 많이 사용된 인기 키워드 목록을 조회합니다.")
    public CommonReponse<List<String>> getPopularKeywords() {
        List<String> popularKeywords = keywordService.retrievePopularKeywords();
        return CommonReponse.success(popularKeywords);
    }

    @PostMapping
    @Operation(summary = "키워드 추가", description = "새로운 키워드를 추가합니다.")
    public CommonReponse<String> addKeyword(@RequestParam Long userId, @RequestParam String keyword) {
        keywordService.registerKeyword(userId, keyword);
        return CommonReponse.success("키워드 추가 성공: " + keyword);
    }

    @PostMapping("/bulk")
    @Operation(summary = "키워드 일괄 등록", description = "다수의 키워드를 한 번에 등록합니다.",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "키워드 목록 예시",
                   content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                           value = "[\"keyword1\", \"keyword2\", \"keyword3\"]"
                       )
                   )
               ))
    public CommonReponse<String> addKeywordsBulk(@RequestParam Long userId, @RequestBody List<String> keywords) {
        keywords.forEach(keyword -> keywordService.registerKeyword(userId, keyword));
        return CommonReponse.success("키워드 일괄 등록 성공");
    }

    @DeleteMapping("/{keywordId}")
    @Operation(summary = "키워드 구독 취소", description = "구독 중인 키워드를 삭제합니다.")
    public CommonReponse<String> unsubscribeKeyword(@RequestParam Long userId, @PathVariable Long keywordId) {
        keywordService.unsubscribeKeyword(userId, keywordId);
        return CommonReponse.success("키워드 삭제 성공: " + keywordId);
    }
}
