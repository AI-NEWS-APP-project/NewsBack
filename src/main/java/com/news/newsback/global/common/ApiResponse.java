package com.news.newsback.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    // 성공 응답 (메시지 + 데이터)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 성공 응답 (데이터 없음)
    public static <Void> ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    // 실패 응답 (메시지만)
    public static <Void> ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // 실패 응답 (메시지 + 에러 상세)
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
