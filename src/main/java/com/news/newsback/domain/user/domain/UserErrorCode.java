package com.news.newsback.domain.user.domain;
import com.news.newsback.global.error.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    // 회원가입 관련
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다.", 409),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다.", 400),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호는 필수입니다.", 400),
    PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT", "비밀번호는 최소 8자 이상이어야 합니다.", 400),

    // 조회 관련
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int status;

    UserErrorCode(String code, String message, int status) {
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
