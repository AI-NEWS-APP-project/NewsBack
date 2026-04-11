package com.news.newsback.domain.user.application;

import com.news.newsback.domain.user.api.AuthResponse;
import com.news.newsback.domain.user.api.UserResponse;
import com.news.newsback.domain.user.domain.SocialProvider;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import com.news.newsback.global.util.JwtTokenProvider;
import com.news.newsback.global.util.StringNormalizeUtil;
import com.news.newsback.infra.oauth.SocialAuthClient;
import com.news.newsback.infra.oauth.SocialAuthClientFactory;
import com.news.newsback.infra.oauth.SocialUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SocialAuthClientFactory socialAuthClientFactory;
    private final SocialAuthClient socialAuthClient; // For Mock in tests

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        @Autowired(required = false) SocialAuthClientFactory socialAuthClientFactory,
        @Autowired(required = false) @Qualifier("mockSocialAuthClient") SocialAuthClient socialAuthClient
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.socialAuthClientFactory = socialAuthClientFactory;
        this.socialAuthClient = socialAuthClient;
    }

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Transactional
    public User signup(String email, String password) {
        String normalizedEmail = StringNormalizeUtil.normalizeEmail(email);

        // 1. 이메일 형식 검증
        validateEmailFormat(normalizedEmail);

        // 2. 비밀번호 검증
        validatePassword(password);

        // 3. 이메일 중복 확인
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 4. 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(password);

        // 5. User 엔티티 생성 및 저장
        User user = User.builder()
            .email(normalizedEmail)
            .password(encodedPassword)
            .socialProvider(SocialProvider.LOCAL.name())
            .build();

        return userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(String email, String password, String fcmToken) {
        String normalizedEmail = StringNormalizeUtil.normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new BusinessException(UserErrorCode.AUTH_INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(UserErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        user.updateFcmToken(fcmToken);
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse socialLogin(String provider, String socialToken, String fcmToken) {
        SocialProvider socialProvider = parseProvider(provider);
        SocialUserInfo socialUserInfo;

        try {
            SocialAuthClient client = (socialAuthClientFactory != null)
                ? socialAuthClientFactory.getClient(socialProvider)
                : socialAuthClient;

            if (client == null) {
                throw new IllegalStateException("소셜 인증 클라이언트를 찾을 수 없습니다.");
            }

            socialUserInfo = client.verify(socialProvider, socialToken);
        } catch (Exception e) {
            throw new BusinessException(UserErrorCode.AUTH_SOCIAL_TOKEN_INVALID, e);
        }

        String normalizedEmail = StringNormalizeUtil.normalizeEmail(socialUserInfo.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> userRepository.save(User.builder()
                .email(normalizedEmail)
                .password(null)
                .socialProvider(socialProvider.name())
                .globalPushEnabled(true)
                .build()));

        user.updateFcmToken(fcmToken);
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            jwtTokenProvider.validateRefreshToken(refreshToken);
        } catch (JwtTokenProvider.TokenExpiredException e) {
            throw new BusinessException(UserErrorCode.AUTH_TOKEN_EXPIRED, e);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(UserErrorCode.AUTH_INVALID_TOKEN, e);
        }

        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null) {
            return;
        }

        user.clearFcmToken();
        user.clearRefreshToken();
        userRepository.save(user);
    }

    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(UserErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(UserErrorCode.PASSWORD_TOO_SHORT);
        }
    }

    private SocialProvider parseProvider(String provider) {
        try {
            SocialProvider socialProvider = SocialProvider.from(provider);
            if (socialProvider == SocialProvider.LOCAL) {
                throw new BusinessException(UserErrorCode.AUTH_PROVIDER_UNSUPPORTED);
            }
            return socialProvider;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(UserErrorCode.AUTH_PROVIDER_UNSUPPORTED, e);
        }
    }

    private AuthResponse issueTokens(User user) {
        JwtTokenProvider.JwtTokenPair tokenPair = jwtTokenProvider.issueTokens(user.getId(), user.getEmail());
        user.updateRefreshToken(tokenPair.refreshToken());
        userRepository.save(user);

        return AuthResponse.builder()
            .accessToken(tokenPair.accessToken())
            .refreshToken(tokenPair.refreshToken())
            .user(UserResponse.from(user))
            .build();
    }
}
