package com.news.newsback.global.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        int status,
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