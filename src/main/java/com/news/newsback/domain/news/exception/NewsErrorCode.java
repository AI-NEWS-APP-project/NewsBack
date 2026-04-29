package com.news.newsback.domain.news.exception;

import com.news.newsback.global.error.ErrorCode;

public enum NewsErrorCode implements ErrorCode {

    // RSS 관련
    RSS_FETCH_FAILED("RSS_FETCH_FAILED", "RSS 피드를 가져오는데 실패했습니다.", 502),
    RSS_PARSE_FAILED("RSS_PARSE_FAILED", "RSS 피드 파싱에 실패했습니다.", 500),
    RSS_INVALID_FORMAT("RSS_INVALID_FORMAT", "잘못된 RSS 피드 형식입니다.", 400),

    // 뉴스 검증 관련
    NEWS_REQUIRED_FIELD_MISSING("NEWS_REQUIRED_FIELD_MISSING", "뉴스 필수 필드가 누락되었습니다.", 400),
    NEWS_ALREADY_EXISTS("NEWS_ALREADY_EXISTS", "이미 존재하는 뉴스입니다.", 409),

    // 뉴스 조회 관련
    NEWS_NOT_FOUND("NEWS_NOT_FOUND", "뉴스를 찾을 수 없습니다.", 404),
    KEYWORD_NOT_FOUND("KEYWORD_NOT_FOUND", "키워드를 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int status;

    NewsErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int status() {
        return status;
    }
}
