package com.news.newsback.unit.domain.user;

import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Test
    @DisplayName("User 생성 시 globalPushEnabled 기본값은 true")
    void 기본값_검증() {
        User user = User.builder()
            .email("test@example.com")
            .password("encoded")
            .socialProvider(SocialProvider.LOCAL)
            .build();

        assertThat(user.getGlobalPushEnabled()).isTrue();
    }

    @Test
    @DisplayName("로그아웃 시 refreshToken만 제거한다")
    void 로그아웃_검증() {
        User user = User.builder()
            .email("test@example.com")
            .password("encoded")
            .socialProvider(SocialProvider.LOCAL)
            .refreshToken("hashed-refresh")
            .build();

        user.logout();

        assertThat(user.getRefreshToken()).isNull();
    }
}
