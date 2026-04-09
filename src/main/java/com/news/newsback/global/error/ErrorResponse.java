package com.news.newsback.global.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "에러 응답 상세")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "AUTH_INVALID_CREDENTIALS")
        String code,
        @Schema(description = "에러 메시지", example = "이메일 또는 비밀번호가 올바르지 않습니다.")
        String message,
        @Schema(description = "HTTP 상태 코드", example = "401")
        int status,
        @Schema(description = "에러 발생 시각", example = "2024-04-07T15:33:56")
        LocalDateTime timestamp
) {
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.code(),
                errorCode.message(),
                errorCode.status(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(String code, String message, int status) {
        return new ErrorResponse(
                code,
                message,
                status,
                LocalDateTime.now()
        );
    }
}