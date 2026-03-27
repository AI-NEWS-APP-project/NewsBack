package com.news.newsback.global.error;
//에러 코드 정의
public interface ErrorCode {
    String code();
    String message();
    int status();
}