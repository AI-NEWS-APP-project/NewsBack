package com.news.newsback.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Tag("integration")
@DisplayName("UserController Integration Test - 회원가입")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("유효한 이메일과 비밀번호로 회원가입 성공 - 201 Created")
    void 회원가입_성공() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.id").exists());

        // DB 검증
        User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 해시되어 저장됨")
    void 비밀번호_해시되어_저장됨() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "plainPassword123");

        // when
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // then
        User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("plainPassword123");
        assertThat(savedUser.getPassword()).startsWith("$2a$"); // BCrypt 해시
    }

    @Test
    @DisplayName("created_at이 자동 생성됨")
    void created_at_자동_생성됨() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        // when
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // then
        User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 409 Conflict")
    void 중복_이메일로_회원가입_실패() throws Exception {
        // given: 기존 사용자 등록
        userRepository.save(User.builder()
            .email("existing@example.com")
            .password(passwordEncoder.encode("password"))
            .build());

        Map<String, String> request = new HashMap<>();
        request.put("email", "existing@example.com");
        request.put("password", "newpassword");

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
        Map<String, String> request = new HashMap<>();
        request.put("email", "invalid-email");
        request.put("password", "password123");

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
        Map<String, Object> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", null);

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
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 대소문자 구분 없이 중복 처리")
    void 이메일_대소문자_중복_처리() throws Exception {
        // given: 소문자 이메일로 기존 사용자 등록
        userRepository.save(User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password"))
            .build());

        Map<String, String> request = new HashMap<>();
        request.put("email", "TEST@EXAMPLE.COM"); // 대문자
        request.put("password", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("비밀번호 최소 길이 경계값 - 8자 정확히 성공")
    void 비밀번호_8자_정확히_성공() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "12345678"); // 정확히 8자

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("비밀번호 최소 길이 미만 - 7자 실패")
    void 비밀번호_7자_실패() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "1234567"); // 7자

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
