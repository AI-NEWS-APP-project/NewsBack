package com.news.newsback.unit.domain.keyword;

import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.keyword.domain.UserKeywordRepository;
import com.news.newsback.domain.keyword.domain.UserKeyword;
import com.news.newsback.domain.keyword.domain.Keyword;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("사용자-키워드 매핑 리포지토리 테스트")
class UserKeywordRepositoryTest {

    @Autowired
    private UserKeywordRepository userKeywordRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private com.news.newsback.domain.user.domain.UserRepository userRepository;

    private Keyword testKeyword;
    private com.news.newsback.domain.user.domain.User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 간 데이터 독립성 보장을 위해 매번 초기화 수행
        userKeywordRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();

        testUser = com.news.newsback.domain.user.domain.User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .socialProvider(com.news.newsback.domain.user.domain.SocialProvider.GOOGLE)
                .refreshToken("refreshToken")
                .globalPushEnabled(true)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        testKeyword = Keyword.builder().name("testKeyword").build();
        testKeyword.setId(1L);
        testKeyword = keywordRepository.save(testKeyword);
    }

    @AfterEach
    void tearDown() {
        userKeywordRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 사용자 ID로 등록된 키워드의 총 개수를 정확하게 조회한다.")
    void countByUserIdTest() {
        // given
        userKeywordRepository.save(createUserKeyword(1L, testKeyword));

        // when
        int count = userKeywordRepository.countByUserId(1L);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 특정 사용자 ID와 키워드 ID로 구독 존재 여부를 확인한다.")
    void existsByUserIdAndKeywordIdTest() {
        // given
        userKeywordRepository.save(createUserKeyword(1L, testKeyword));

        // when
        boolean exists = userKeywordRepository.existsByUserIdAndKeywordId(1L, testKeyword.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("성공: 사용자 ID와 키워드 ID를 기준으로 구독 데이터를 삭제한다.")
    void deleteByUserIdAndKeywordIdTest() {
        // given
        userKeywordRepository.save(createUserKeyword(1L, testKeyword));

        // when
        userKeywordRepository.deleteByUserIdAndKeywordId(1L, testKeyword.getId());
        boolean exists = userKeywordRepository.existsByUserIdAndKeywordId(1L, testKeyword.getId());

        // then
        assertThat(exists).isFalse();
    }

    private UserKeyword createUserKeyword(Long userId, Keyword keyword) {
        return UserKeyword.builder()
                .userId(userId)
                .keyword(keyword)
                .build();
    }
}
