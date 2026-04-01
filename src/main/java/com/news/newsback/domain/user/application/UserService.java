package com.news.newsback.domain.user.application;

import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import com.news.newsback.global.util.StringNormalizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Transactional
    public User signup(String email, String password) {
        // 1. 이메일 형식 검증
        validateEmailFormat(email);

        // 2. 비밀번호 검증
        validatePassword(password);

        // 3. 이메일 정규화 (StringNormalizeUtil 사용)
        String normalizedEmail = StringNormalizeUtil.normalizeEmail(email);

        // 4. 이메일 중복 확인
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 5. 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(password);

        // 6. User 엔티티 생성 및 저장
        User user = User.builder()
            .email(normalizedEmail)
            .password(encodedPassword)
            .build();

        return userRepository.save(user);
    }

    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다");
        }
    }
}
