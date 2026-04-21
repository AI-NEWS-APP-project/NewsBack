package com.news.newsback.domain.keyword.exception;

import com.news.newsback.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeywordErrorCode implements ErrorCode {
    // 유저 조회
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),

    // 키워드 개수 제한
    MAX_KEYWORDS_EXCEEDED("최대 키워드 개수를 초과했습니다."),

    // 키워드 중복
    DUPLICATE_KEYWORD("중복된 키워드입니다."),

    KEYWORD_NOT_FOUND("키워드를 찾을 수 없습니다.");

    private final String message;

    @Override
    public String message() {
        return message;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public int status() {
        return 400;
    }
}
