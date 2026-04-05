package com.news.newsback.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.newsback.config.TestSecurityConfig;
import com.news.newsback.domain.user.api.AuthResponse;
import com.news.newsback.domain.user.api.UserController;
import com.news.newsback.domain.user.api.UserRequest;
import com.news.newsback.domain.user.api.UserResponse;
import com.news.newsback.domain.user.application.UserService;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(TestSecurityConfig.class)
@Tag("unit")
@DisplayName("UserController Unit Test")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Nested
    @DisplayName("회원가입 API")
    class SignupApiTest {

        @Test
        @DisplayName("유효한 이메일과 비밀번호로 회원가입 성공 - 201 Created")
        void 회원가입_성공_테스트1() throws Exception {
            // given
            String email = "test@example.com";
            String password = "password123";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .socialProvider("LOCAL")
                .build();

            given(userService.signup(anyString(), anyString())).willReturn(mockUser);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("서비스 호출 및 응답 body 검증")
        void 회원가입_성공_테스트2() throws Exception {
            // given
            String email = "test@example.com";
            String password = "password123";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .socialProvider("LOCAL")
                .build();

            given(userService.signup(anyString(), anyString())).willReturn(mockUser);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

            verify(userService).signup(anyString(), anyString());
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 409 Conflict")
        void 중복_이메일로_회원가입_실패() throws Exception {
            // given
            String email = "existing@example.com";
            String password = "password123";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            given(userService.signup(anyString(), anyString()))
                .willThrow(new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS));

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400 Bad Request")
        void 이메일_형식_오류() throws Exception {
            // given
            String email = "invalid-email";
            String password = "password123";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("비밀번호가 null이면 400 Bad Request")
        void 비밀번호_null_오류() throws Exception {
            // given
            String email = "test@example.com";
            String password = null;
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 빈 문자열이면 400 Bad Request")
        void 비밀번호_빈_문자열_오류() throws Exception {
            // given
            String email = "test@example.com";
            String password = "";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 최소 길이 미만 - 7자 실패")
        void 비밀번호_7자_실패() throws Exception {
            // given
            String email = "test@example.com";
            String password = "1234567";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 최소 길이 경계값 - 8자 정확히 성공")
        void 비밀번호_8자_정확히_성공() throws Exception {
            // given
            String email = "test@example.com";
            String password = "12345678";
            UserRequest.Signup request = new UserRequest.Signup(email, password);

            User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .socialProvider("LOCAL")
                .build();

            given(userService.signup(anyString(), anyString())).willReturn(mockUser);

            // when & then
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class LoginApiTest {

        @Test
        @DisplayName("유효한 이메일과 비밀번호로 로그인 성공 - 200 OK")
        void 로그인_성공() throws Exception {
            // given
            UserRequest.Login request = new UserRequest.Login("test@example.com", "password123", "new-fcm-token");

            User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .socialProvider("LOCAL")
                .fcmToken("new-fcm-token")
                .globalPushEnabled(true)
                .build();

            AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(UserResponse.from(user))
                .build();

            given(userService.login(eq("test@example.com"), eq("password123"), eq("new-fcm-token")))
                .willReturn(response);

            // when & then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 401 Unauthorized")
        void 존재하지_않는_이메일_로그인_실패() throws Exception {
            // given
            UserRequest.Login request = new UserRequest.Login("unknown@example.com", "password123", null);
            given(userService.login(anyString(), anyString(), isNull()))
                .willThrow(new BusinessException(UserErrorCode.AUTH_INVALID_CREDENTIALS));

            // when & then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
        }

        @Test
        @DisplayName("비밀번호 불일치 시 401 Unauthorized")
        void 비밀번호_불일치_로그인_실패() throws Exception {
            // given
            UserRequest.Login request = new UserRequest.Login("test@example.com", "wrong-password", "fcm-token");
            given(userService.login(anyString(), anyString(), anyString()))
                .willThrow(new BusinessException(UserErrorCode.AUTH_INVALID_CREDENTIALS));

            // when & then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("소셜 로그인 API")
    class SocialLoginApiTest {

        @Test
        @DisplayName("구글 로그인 성공 - 200 OK")
        void 구글_로그인_성공() throws Exception {
            // given
            UserRequest.SocialLogin request = new UserRequest.SocialLogin("mock-valid:test@example.com", "fcm-token");
            User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .socialProvider("GOOGLE")
                .fcmToken("fcm-token")
                .globalPushEnabled(true)
                .build();
            AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(UserResponse.from(user))
                .build();
            given(userService.socialLogin(eq("google"), anyString(), anyString())).willReturn(response);

            // when & then
            mockMvc.perform(post("/auth/social/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("카카오 로그인 성공 - 200 OK")
        void 카카오_로그인_성공() throws Exception {
            // given
            UserRequest.SocialLogin request = new UserRequest.SocialLogin("mock-valid:kakao@example.com", null);
            User user = User.builder()
                .id(2L)
                .email("kakao@example.com")
                .socialProvider("KAKAO")
                .globalPushEnabled(true)
                .build();
            AuthResponse response = AuthResponse.builder()
                .accessToken("access-token-2")
                .refreshToken("refresh-token-2")
                .user(UserResponse.from(user))
                .build();
            given(userService.socialLogin(eq("kakao"), anyString(), isNull())).willReturn(response);

            // when & then
            mockMvc.perform(post("/auth/social/kakao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("kakao@example.com"));
        }

        @Test
        @DisplayName("잘못된 소셜 토큰으로 로그인 시 401 Unauthorized")
        void 잘못된_토큰_소셜_로그인_실패() throws Exception {
            // given
            UserRequest.SocialLogin request = new UserRequest.SocialLogin("invalid-token", "fcm-token");
            given(userService.socialLogin(eq("google"), anyString(), anyString()))
                .willThrow(new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID));

            // when & then
            mockMvc.perform(post("/auth/social/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class LogoutApiTest {

        @Test
        @DisplayName("로그아웃 성공 - 200 OK")
        void 로그아웃_성공() throws Exception {
            UserRequest.Logout request = new UserRequest.Logout("refresh-token");

            mockMvc.perform(post("/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(userService).logout("refresh-token");
        }

        @Test
        @DisplayName("로그아웃 요청에 refreshToken이 비어있으면 400")
        void 로그아웃_요청값_검증_실패() throws Exception {
            UserRequest.Logout request = new UserRequest.Logout("");

            mockMvc.perform(post("/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }
}
