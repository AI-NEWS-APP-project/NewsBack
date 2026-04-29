package com.news.newsback.domain.news.exception;

import com.news.newsback.global.error.BusinessException;
import com.news.newsback.global.error.ErrorCode;

public class NewsException extends BusinessException {
    public NewsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NewsException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
