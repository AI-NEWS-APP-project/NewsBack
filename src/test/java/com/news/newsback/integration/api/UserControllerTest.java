package com.news.newsback.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.newsback.config.TestSecurityConfig;
import com.news.newsback.domain.user.api.UserController;
import com.news.newsback.domain.user.api.UserRequest;
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
    class 회원가입_테스트 {

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
    class 로그인_테스트 {

        @Test
        @DisplayName("유효한 이메일과 비밀번호로 로그인 성공 - 200 OK")
        void 로그인_성공() throws Exception {
            // given

            // when & then
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 401 Unauthorized")
        void 존재하지_않는_이메일_로그인_실패() throws Exception {
            // given

            // when & then
        }

        @Test
        @DisplayName("비밀번호 불일치 시 401 Unauthorized")
        void 비밀번호_불일치_로그인_실패() throws Exception {
            // given

            // when & then
        }
    }

    @Nested
    @DisplayName("소셜 로그인 API")
    class 소셜로그인_테스트 {

        @Test
        @DisplayName("구글 로그인 성공 - 200 OK")
        void 구글_로그인_성공() throws Exception {
            // given

            // when & then
        }

        @Test
        @DisplayName("카카오 로그인 성공 - 200 OK")
        void 카카오_로그인_성공() throws Exception {
            // given

            // when & then
        }

        @Test
        @DisplayName("잘못된 소셜 토큰으로 로그인 시 401 Unauthorized")
        void 잘못된_토큰_소셜_로그인_실패() throws Exception {
            // given

            // when & then
        }
    }
}
