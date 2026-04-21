package com.news.newsback.unit.application.keyword;

import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.keyword.domain.UserKeyword;
import com.news.newsback.domain.keyword.domain.UserKeywordRepository;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.domain.keyword.application.KeywordService;
import com.news.newsback.domain.keyword.exception.KeywordErrorCode;
import com.news.newsback.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("키워드 서비스 비즈니스 로직 단위 테스트")
class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserKeywordRepository userKeywordRepository;

    @InjectMocks
    private KeywordService keywordService;

    @BeforeEach
    void setUp() {
        // 테스트 수행용 Mock 유저 데이터 설정
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .socialProvider(com.news.newsback.domain.user.domain.SocialProvider.GOOGLE)
                .fcmToken("fcmToken")
                .refreshToken("refreshToken")
                .globalPushEnabled(true)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // 기본 키워드 설정
        Keyword keyword = new Keyword("ai");
        when(keywordRepository.findByName(anyString())).thenReturn(Optional.of(keyword));
        when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);

        // 유저 키워드 설정
        when(userKeywordRepository.countByUserId(anyLong())).thenReturn(0);
        when(userKeywordRepository.existsByUserIdAndKeywordId(anyLong(), anyLong())).thenReturn(false);
    }

    @Nested
    @DisplayName("키워드 등록 검증")
    class KeywordRegistration {

        @Test
        @DisplayName("성공: 유효한 키워드가 정규화 과정을 거쳐 정상적으로 등록된다.")
        void successfulKeywordRegistrationTest() {
            // given
            Long userId = 1L;
            String rawKeyword = " AI ";

            // when
            keywordService.registerKeyword(userId, rawKeyword);

            // then: 매핑 테이블(UserKeyword)에 데이터 저장이 호출되었는지 검증
            verify(userKeywordRepository).save(any(UserKeyword.class));
        }

        @Test
        @DisplayName("실패: 유저당 보유 가능한 최대 키워드(5개)를 초과하면 예외가 발생한다.")
        void keywordRegistrationFailureTestExceedsLimit() {
            // given: 이미 5개의 키워드를 구독 중인 상황
            when(userKeywordRepository.countByUserId(1L)).thenReturn(5);

            // when & then
            assertThrows(BusinessException.class, () -> keywordService.registerKeyword(1L, "newKeyword"),
                    KeywordErrorCode.MAX_KEYWORDS_EXCEEDED.name());
        }

        @Test
        @DisplayName("실패: 이미 구독 중인 동일 키워드 등록 시 중복 예외가 발생한다.")
        void keywordRegistrationFailureTestDuplicate() {
            // given: 이미 동일한 키워드(ai)가 존재하고 구독 중인 상태 설정
            Keyword existingKeyword = new Keyword("ai");
            existingKeyword.setId(1L);
            when(keywordRepository.findByName("ai")).thenReturn(Optional.of(existingKeyword));
            when(userKeywordRepository.existsByUserIdAndKeywordId(1L, 1L)).thenReturn(true);

            // when & then: 중복 등록 시도 시 예외 발생 검증
            assertThrows(BusinessException.class, () -> keywordService.registerKeyword(1L, "ai"),
                    KeywordErrorCode.DUPLICATE_KEYWORD.name());
        }
    }

    @Nested
    @DisplayName("키워드 구독 해지 검증")
    class KeywordUnsubscription {

        @Test
        @DisplayName("성공: 구독 중인 키워드를 해지하면 정상적으로 삭제 처리가 된다.")
        void successfulKeywordUnsubscriptionTest() {
            // given
            Long userId = 1L;
            String rawKeyword = " AI ";
            keywordService.registerKeyword(userId, rawKeyword);

            // when
            keywordService.unsubscribeKeyword(userId, rawKeyword);

            // then
            verify(keywordRepository).delete(any(Keyword.class));
        }
    }

    @Nested
    @DisplayName("인기 키워드 조회 검증")
    class PopularKeywordRetrieval {

        @Test
        @DisplayName("성공: 가장 많이 사용된 인기 키워드 목록을 정상적으로 반환한다.")
        void successfulPopularKeywordRetrievalTest() {
            // given: 인기 키워드 데이터
            when(keywordRepository.findPopularKeywords()).thenReturn(List.of("ai", "ml"));

            // when
            List<String> popularKeywords = keywordService.retrievePopularKeywords();

            // then: 기대한 키워드 목록과 일치하는지 확인
            assertThat(popularKeywords).containsExactly("ai", "ml");
        }
    }
}