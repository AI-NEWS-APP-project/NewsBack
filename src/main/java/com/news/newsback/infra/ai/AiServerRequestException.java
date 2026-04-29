package com.news.newsback.infra.ai;

public class AiServerRequestException extends RuntimeException {

    public AiServerRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
