package com.news.newsback.unit.application.user;

import com.news.newsback.domain.user.application.UserService;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트 - 회원가입")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유효한 이메일과 비밀번호로 회원가입 성공")
    void 유효한_이메일과_비밀번호로_회원가입_성공() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "hashedPassword123";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                .id(1L)
                .email(user.getEmail())
                .password(user.getPassword())
                .globalPushEnabled(user.getGlobalPushEnabled())
                .build();
        });

        // when
        User savedUser = userService.signup(email, rawPassword);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getGlobalPushEnabled()).isTrue();

        // verify
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외 발생")
    void 중복된_이메일로_회원가입시_예외_발생() {
        // given
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(email, password))
            .isInstanceOf(new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS).getClass())
            .hasMessageContaining("이미 존재하는 이메일입니다.");

        // verify - save가 호출되지 않아야 함
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 예외 발생")
    void 이메일_형식이_올바르지_않으면_예외_발생() {
        // given
        String invalidEmail = "invalid-email";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> userService.signup(invalidEmail, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일 형식이 올바르지 않습니다");

        // verify - repository 호출 안 됨
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("비밀번호가 null 또는 빈 문자열이면 예외 발생")
    void 비밀번호가_null_또는_빈_문자열이면_예외_발생(String password) {
        // given
        String email = "test@example.com";

        // when & then
        assertThatThrownBy(() -> userService.signup(email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비밀번호는 필수입니다");

        // verify
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 최소 길이 미만이면 예외 발생")
    void 비밀번호_최소_길이_미만이면_예외_발생() {
        // given
        String email = "test@example.com";
        String shortPassword = "123"; // 8자 미만

        // when & then
        assertThatThrownBy(() -> userService.signup(email, shortPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비밀번호는 최소 8자 이상이어야 합니다");

        // verify
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test@Example.com", "TEST@EXAMPLE.COM", "test@EXAMPLE.com"})
    @DisplayName("이메일 대소문자 구분 없이 중복 처리")
    void 이메일_대소문자_구분_없이_중복_처리(String email) {
        // given
        String password = "password123";
        String normalizedEmail = email.toLowerCase();

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(email, password))
            .isInstanceOf(new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS).getClass());

        // verify - 이메일이 소문자로 변환되어 검증됨
        verify(userRepository, times(1)).existsByEmail(normalizedEmail);
    }

    @Test
    @DisplayName("비밀번호 최소 길이 경계값 테스트 - 8자 정확히")
    void 비밀번호가_정확히_8자일때_회원가입_성공() {
        // given
        String email = "test@example.com";
        String password = "12345678"; // 정확히 8자
        String encodedPassword = "encoded12345678";

        when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = userService.signup(email, password);

        // then
        assertThat(savedUser).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호가 해시되어 저장됨")
    void 비밀번호가_해시되어_저장됨() {
        // given
        String email = "test@example.com";
        String rawPassword = "plainPassword123";
        String hashedPassword = "$2a$10$hashedPassword...";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = userService.signup(email, rawPassword);

        // then
        assertThat(savedUser.getPassword()).isEqualTo(hashedPassword);
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword); // 원본 비밀번호와 달라야 함

        // verify
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("회원가입 시 created_at이 자동 생성됨")
    void created_at_자동_생성됨(){
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                .id(1L)
                .email(user.getEmail())
                .password(user.getPassword())
                .globalPushEnabled(user.getGlobalPushEnabled())
                .createdAt(java.time.LocalDateTime.now()) // DB에서 자동 생성 시뮬레이션
                .build();
        });

        // when
        User savedUser = userService.signup(email, password);

        // then
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBefore(java.time.LocalDateTime.now().plusSeconds(1));
    }
}
