package com.news.newsback.unit.application.user;

import com.news.newsback.application.alarm.FcmTokenService;
import com.news.newsback.domain.user.api.AuthResponse;
import com.news.newsback.domain.user.application.UserService;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import com.news.newsback.global.util.JwtTokenProvider;
import com.news.newsback.infra.oauth.SocialAuthClient;
import com.news.newsback.infra.oauth.SocialUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SocialAuthClient socialAuthClient;

    @Mock
    private FcmTokenService fcmTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 시 이메일은 정규화되고 비밀번호는 해싱된다")
    void 회원가입_성공() {
        String email = "Test@Example.com";
        String password = "password123";

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.signup(email, password);

        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.getSocialProvider()).isEqualTo(SocialProvider.LOCAL);

        verify(userRepository).existsByEmailIgnoreCase("test@example.com");
        verify(passwordEncoder).encode(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "TEST@EXAMPLE.COM"})
    @DisplayName("이메일 대소문자 무시 중복 체크")
    void 이메일_중복_체크(String email) {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(email, "password123"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(UserErrorCode.EMAIL_ALREADY_EXISTS.message());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("비밀번호 null/빈문자열은 예외")
    void 비밀번호_null_or_empty(String password) {
        assertThatThrownBy(() -> userService.signup("test@example.com", password))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(UserErrorCode.INVALID_PASSWORD.message());
    }

    @Test
    @DisplayName("비밀번호 최소 길이 미만은 예외")
    void 비밀번호_최소길이_검증() {
        assertThatThrownBy(() -> userService.signup("test@example.com", "1234567"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(UserErrorCode.PASSWORD_TOO_SHORT.message());
    }

    @Test
    @DisplayName("credential 로그인 성공 시 토큰 발급 및 FCM 토큰 업데이트")
    void 로그인_성공() {
        User user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encoded")
            .socialProvider(SocialProvider.LOCAL)
            .build();

        JwtTokenProvider.JwtTokenPair tokenPair =
            new JwtTokenProvider.JwtTokenPair("access", "refresh", LocalDateTime.now().plusDays(14));

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.issueTokens(1L, "test@example.com")).thenReturn(tokenPair);
        when(passwordEncoder.encode("refresh")).thenReturn("hashed-refresh");

        AuthResponse response = userService.login("test@example.com", "password123", "fcm-token");

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(user.getFcmToken()).isEqualTo("fcm-token");
        assertThat(user.getRefreshToken()).isEqualTo("hashed-refresh");
        verify(fcmTokenService).registerToken(1L, "fcm-token");
    }

    @Test
    @DisplayName("credential 로그인 실패 시 메시지는 통일")
    void 로그인_실패() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("unknown@example.com", "password", null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(UserErrorCode.AUTH_INVALID_CREDENTIALS.message());
    }

    @Test
    @DisplayName("소셜 로그인 성공 시 신규 사용자 생성 후 토큰 발급")
    void 소셜로그인_신규회원() {
        JwtTokenProvider.JwtTokenPair tokenPair =
            new JwtTokenProvider.JwtTokenPair("access", "refresh", LocalDateTime.now().plusDays(14));

        when(socialAuthClient.verify(SocialProvider.GOOGLE, "mock-valid:new@example.com"))
            .thenReturn(new SocialUserInfo("new@example.com"));
        when(userRepository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                .id(10L)
                .email(user.getEmail())
                .password(user.getPassword())
                .socialProvider(user.getSocialProvider())
                .fcmToken(user.getFcmToken())
                .globalPushEnabled(user.getGlobalPushEnabled())
                .build();
        });
        when(jwtTokenProvider.issueTokens(10L, "new@example.com")).thenReturn(tokenPair);
        when(passwordEncoder.encode("refresh")).thenReturn("hashed-refresh");

        AuthResponse response = userService.socialLogin("google", "mock-valid:new@example.com", null);

        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("지원하지 않는 소셜 provider는 예외")
    void 소셜로그인_provider_예외() {
        assertThatThrownBy(() -> userService.socialLogin("naver", "token", null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(UserErrorCode.AUTH_PROVIDER_UNSUPPORTED.message());
    }

    @Test
    @DisplayName("로그아웃 성공 시 refresh 토큰 세션 삭제 및 FCM 토큰 제거")
    void 로그아웃_성공() {
        User user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encoded")
            .socialProvider(SocialProvider.LOCAL)
            .fcmToken("fcm-token")
            .refreshToken("hashed-refresh-token")
            .build();

        when(jwtTokenProvider.extractValidRefreshUserId("refresh-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("refresh-token", "hashed-refresh-token")).thenReturn(true);

        userService.logout("refresh-token");

        assertThat(user.getFcmToken()).isNull();
        assertThat(user.getRefreshToken()).isNull();
        verify(fcmTokenService).disableToken(1L, "fcm-token");
    }

    @Test
    @DisplayName("로그아웃에서 만료된 refresh 토큰은 예외")
    void 로그아웃_만료토큰() {
        doThrow(new JwtTokenProvider.TokenExpiredException("expired"))
            .when(jwtTokenProvider).extractValidRefreshUserId("expired-token");

        assertThatThrownBy(() -> userService.logout("expired-token"))
            .isInstanceOf(JwtTokenProvider.TokenExpiredException.class);
    }

    @Test
    @DisplayName("로그아웃에서 유효하지 않은 refresh 토큰은 예외")
    void 로그아웃_유효하지않은토큰() {
        doThrow(new JwtTokenProvider.InvalidTokenException("invalid"))
            .when(jwtTokenProvider).extractValidRefreshUserId("invalid-token");

        assertThatThrownBy(() -> userService.logout("invalid-token"))
            .isInstanceOf(JwtTokenProvider.InvalidTokenException.class);
    }

    @Test
    @DisplayName("refresh 성공 시 입력 토큰은 해시 검증되고 새 토큰으로 회전된다")
    void refresh_성공() {
        User user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encoded")
            .socialProvider(SocialProvider.LOCAL)
            .fcmToken("fcm-token")
            .refreshToken("hashed-old-refresh")
            .build();

        JwtTokenProvider.JwtTokenPair tokenPair =
            new JwtTokenProvider.JwtTokenPair("new-access", "new-refresh", LocalDateTime.now().plusDays(14));

        when(jwtTokenProvider.extractValidRefreshUserId("old-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-refresh", "hashed-old-refresh")).thenReturn(true);
        when(jwtTokenProvider.issueTokens(1L, "test@example.com")).thenReturn(tokenPair);
        when(passwordEncoder.encode("new-refresh")).thenReturn("hashed-new-refresh");

        AuthResponse response = userService.refresh("old-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        assertThat(user.getRefreshToken()).isEqualTo("hashed-new-refresh");
    }
}
