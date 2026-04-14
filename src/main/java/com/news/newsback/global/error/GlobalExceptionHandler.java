package com.news.newsback.global.error;

import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.global.util.JwtTokenProvider;
import com.news.newsback.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.status())
            .body(ApiResponse.error(errorCode.message(), ErrorResponse.from(errorCode)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(e.getMessage(), ErrorResponse.of("INVALID_ARGUMENT", e.getMessage(), HttpStatus.BAD_REQUEST.value())));
    }

    @ExceptionHandler(JwtTokenProvider.TokenExpiredException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTokenExpiredException(JwtTokenProvider.TokenExpiredException e) {
        log.error("TokenExpiredException: {}", e.getMessage());
        return ResponseEntity
            .status(UserErrorCode.AUTH_TOKEN_EXPIRED.status())
            .body(ApiResponse.error(
                UserErrorCode.AUTH_TOKEN_EXPIRED.message(),
                ErrorResponse.from(UserErrorCode.AUTH_TOKEN_EXPIRED)
            ));
    }

    @ExceptionHandler(JwtTokenProvider.InvalidTokenException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidTokenException(JwtTokenProvider.InvalidTokenException e) {
        log.error("InvalidTokenException: {}", e.getMessage());
        return ResponseEntity
            .status(UserErrorCode.AUTH_INVALID_TOKEN.status())
            .body(ApiResponse.error(
                UserErrorCode.AUTH_INVALID_TOKEN.message(),
                ErrorResponse.from(UserErrorCode.AUTH_INVALID_TOKEN)
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> data = new HashMap<>();
        data.put("error", ErrorResponse.of("VALIDATION_ERROR", "입력값 검증 실패", HttpStatus.BAD_REQUEST.value()));
        data.put("fields", errors);

        log.error("Validation failed: {}", errors);
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("입력값 검증 실패", data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnexpectedException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다",
                ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }
}
