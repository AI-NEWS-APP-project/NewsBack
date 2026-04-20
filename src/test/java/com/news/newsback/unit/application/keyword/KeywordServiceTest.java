package com.news.newsback.unit.application.keyword;

import com.news.newsback.domain.keyword.Keyword;
import com.news.newsback.domain.keyword.KeywordRepository;
import com.news.newsback.domain.user.User;
import com.news.newsback.domain.user.UserRepository;
import com.news.newsback.application.keyword.KeywordService;
import com.news.newsback.application.keyword.exception.KeywordErrorCode;
import com.news.newsback.application.keyword.exception.BusinessException;
import com.news.newsback.domain.keyword.util.KeywordNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DisplayName("KeywordService 테스트")
class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KeywordService keywordService;

    public KeywordServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("키워드 등록")
    class 키워드_등록 {

        @Test
        @DisplayName("키워드_등록_성공_테스트")
        void 키워드_등록_성공_테스트() {
            // given
            User user = new User("testUser");
            String rawKeyword = " AI ";
            String normalizedKeyword = KeywordNormalizer.normalize(rawKeyword);
            Keyword keyword = new Keyword(normalizedKeyword);

            given(keywordRepository.findByKeyword(normalizedKeyword)).willReturn(Optional.of(keyword));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // when
            keywordService.registerKeyword(user.getId(), rawKeyword);

            // then
            // 성공적으로 등록되었는지 검증 (구체적인 검증 로직은 실제 구현 시 추가)
        }

        @Test
        @DisplayName("키워드_등록_실패_테스트_5개_초과")
        void 키워드_등록_실패_테스트_5개_초과() {
            // given
            User user = new User("testUser");
            given(user.getKeywords()).willReturn(List.of("keyword1", "keyword2", "keyword3", "keyword4", "keyword5"));

            // when & then
            assertThatThrownBy(() -> keywordService.registerKeyword(user.getId(), "newKeyword"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(KeywordErrorCode.MAX_KEYWORDS_EXCEEDED.name());
        }

        @Test
        @DisplayName("키워드_등록_실패_테스트_중복")
        void 키워드_등록_실패_테스트_중복() {
            // given
            User user = new User("testUser");
            String rawKeyword = " AI ";
            String normalizedKeyword = KeywordNormalizer.normalize(rawKeyword);
            Keyword keyword = new Keyword(normalizedKeyword);

            given(keywordRepository.findByKeyword(normalizedKeyword)).willReturn(Optional.of(keyword));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(user.getKeywords()).willReturn(List.of(normalizedKeyword));

            // when & then
            assertThatThrownBy(() -> keywordService.registerKeyword(user.getId(), rawKeyword))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(KeywordErrorCode.DUPLICATE_KEYWORD.name());
        }
    }

    @Nested
    @DisplayName("키워드 구독 해제")
    class 키워드_구독_해제 {

        @Test
        @DisplayName("키워드_구독_해제_성공_테스트")
        void 키워드_구독_해제_성공_테스트() {
            // given
            User user = new User("testUser");
            String rawKeyword = " AI ";
            String normalizedKeyword = KeywordNormalizer.normalize(rawKeyword);
            Keyword keyword = new Keyword(normalizedKeyword);

            given(keywordRepository.findByKeyword(normalizedKeyword)).willReturn(Optional.of(keyword));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // when
            keywordService.unsubscribeKeyword(user.getId(), rawKeyword);

            // then
            // 성공적으로 구독 해제되었는지 검증 (구체적인 검증 로직은 실제 구현 시 추가)
        }
    }

    @Nested
    @DisplayName("인기 키워드 조회")
    class 인기_키워드_조회 {

        @Test
        @DisplayName("인기_키워드_조회_성공_테스트")
        void 인기_키워드_조회_성공_테스트() {
            // given
            // 인기 키워드 조회를 위한 가짜 데이터 설정

            // when
            // 인기 키워드 조회 메서드 호출

            // then
            // 조회 결과 검증
        }
    }
}
