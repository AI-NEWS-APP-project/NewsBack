package com.news.newsback.unit.domain.keyword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KeywordNormalizer 테스트")
class KeywordNormalizerTest {

    @Test
    @DisplayName("키워드_정규화_테스트")
    void 키워드_정규화_테스트() {
        // 입력값
        String input1 = " AI ";
        String input2 = "Samsung";
        String input3 = "반도체   투자";

        // 결과값
        String result1 = null;
        String result2 = null;
        String result3 = null;

        // 정규화 처리
        assertThat(result1).isEqualTo("ai");
        assertThat(result2).isEqualTo("samsung");
        assertThat(result3).isEqualTo("반도체 투자");
    }
}
