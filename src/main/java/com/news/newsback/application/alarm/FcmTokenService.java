package com.news.newsback.application.alarm;

import com.news.newsback.domain.alarm.model.FcmToken;
import com.news.newsback.domain.alarm.repository.FcmTokenRepository;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserErrorCode;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerToken(Long userId, String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        fcmToken -> fcmToken.reactivate(user),
                        () -> fcmTokenRepository.save(FcmToken.create(user, token))
                );
    }

    @Transactional
    public void disableToken(Long userId, String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        fcmTokenRepository.findByUserIdAndToken(userId, token)
                .ifPresent(FcmToken::disable);
    }

    @Transactional
    public void disableInvalidTokens(Collection<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        fcmTokenRepository.findByTokenIn(tokens)
                .forEach(FcmToken::disable);
    }
}
