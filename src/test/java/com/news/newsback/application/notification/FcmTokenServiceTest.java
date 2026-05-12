package com.news.newsback.application.notification;

import com.news.newsback.domain.notification.model.FcmToken;
import com.news.newsback.domain.notification.repository.FcmTokenRepository;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmTokenService 단위 테스트")
class FcmTokenServiceTest {

    @InjectMocks
    private FcmTokenService fcmTokenService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("FCM 토큰을 신규 등록한다")
    void FCM_토큰_신규_등록() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fcmTokenRepository.findByToken("token-1")).thenReturn(Optional.empty());

        fcmTokenService.registerToken(1L, "token-1");

        verify(fcmTokenRepository).save(org.mockito.ArgumentMatchers.argThat(token ->
                token.getUser().equals(user)
                        && token.getToken().equals("token-1")
                        && token.isEnabled()
        ));
    }

    @Test
    @DisplayName("기존 FCM 토큰을 재등록하면 사용자와 활성 상태를 갱신한다")
    void FCM_토큰_재등록_갱신() {
        User oldUser = user(1L);
        User newUser = user(2L);
        FcmToken token = FcmToken.create(oldUser, "token-1");
        token.disable();

        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(fcmTokenRepository.findByToken("token-1")).thenReturn(Optional.of(token));

        fcmTokenService.registerToken(2L, "token-1");

        assertThat(token.getUser()).isEqualTo(newUser);
        assertThat(token.isEnabled()).isTrue();
        verify(fcmTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("blank 토큰은 등록하지 않는다")
    void BLANK_토큰_등록하지_않음() {
        fcmTokenService.registerToken(1L, " ");

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        verify(fcmTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 예외를 던진다")
    void FCM_토큰_등록_사용자_없음() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fcmTokenService.registerToken(99L, "token-1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 토큰을 비활성화한다")
    void FCM_토큰_비활성화() {
        FcmToken token = FcmToken.create(user(1L), "token-1");
        when(fcmTokenRepository.findByUserIdAndToken(1L, "token-1")).thenReturn(Optional.of(token));

        fcmTokenService.disableToken(1L, "token-1");

        assertThat(token.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("실패 토큰 목록을 비활성화한다")
    void 실패_토큰_목록_비활성화() {
        FcmToken token = FcmToken.create(user(1L), "token-1");
        when(fcmTokenRepository.findByTokenIn(List.of("token-1"))).thenReturn(List.of(token));

        fcmTokenService.disableInvalidTokens(List.of("token-1"));

        assertThat(token.isEnabled()).isFalse();
    }

    private User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.LOCAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
