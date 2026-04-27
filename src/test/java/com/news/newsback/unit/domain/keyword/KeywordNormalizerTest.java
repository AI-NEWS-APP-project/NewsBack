package com.news.newsback.unit.domain.keyword;

import com.news.newsback.domain.keyword.util.KeywordNormalizer;
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

        String result1 = KeywordNormalizer.normalize(input1);
        String result2 = KeywordNormalizer.normalize(input2);
        String result3 = KeywordNormalizer.normalize(input3);

        // 결과값
        assertThat(result1).isEqualTo("ai");
        assertThat(result2).isEqualTo("samsung");
        assertThat(result3).isEqualTo("반도체 투자");
    }
}
