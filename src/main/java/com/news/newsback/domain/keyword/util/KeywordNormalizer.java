package com.news.newsback.domain.keyword.util;

public class KeywordNormalizer {

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        // 앞뒤 공백 제거, 소문자 정규화
        return input.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
