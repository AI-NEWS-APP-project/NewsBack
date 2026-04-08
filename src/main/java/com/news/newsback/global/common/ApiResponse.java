package com.news.newsback.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답")
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지 (실패 시 원인 등)", example = "요청에 성공했습니다")
    private String message;

    @Schema(description = "응답 데이터")
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
