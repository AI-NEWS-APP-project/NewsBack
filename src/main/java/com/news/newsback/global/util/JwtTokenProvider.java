package com.news.newsback.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String USER_EMAIL_CLAIM = "email";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-days:14}")
    private long refreshTokenExpirationDays;

    private final ObjectMapper objectMapper;
    private byte[] secretKeyBytes;

    public JwtTokenProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtTokenPair issueTokens(Long userId, String email) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiresAt = now.plusMinutes(accessTokenExpirationMinutes);
        LocalDateTime refreshExpiresAt = now.plusDays(refreshTokenExpirationDays);

        String accessToken = buildToken(userId, email, "ACCESS", accessExpiresAt);
        String refreshToken = buildToken(userId, email, "REFRESH", refreshExpiresAt);

        return new JwtTokenPair(accessToken, refreshToken, refreshExpiresAt);
    }

    public Long extractValidRefreshUserId(String token) {
        Map<String, Object> claims = parseClaims(token);
        validateTokenType(claims, "REFRESH");
        return Long.valueOf(String.valueOf(claims.get("sub")));
    }

    private String buildToken(Long userId, String email, String tokenType, LocalDateTime expiresAt) {
        long nowEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long expEpoch = expiresAt.toEpochSecond(ZoneOffset.UTC);

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put(USER_EMAIL_CLAIM, email);
        payload.put(TOKEN_TYPE_CLAIM, tokenType);
        payload.put("iat", nowEpoch);
        payload.put("exp", expEpoch);

        String encodedHeader = base64UrlEncode(toJson(header));
        String encodedPayload = base64UrlEncode(toJson(payload));
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    private Map<String, Object> parseClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        String signedPart = parts[0] + "." + parts[1];
        String expectedSignature = sign(signedPart);
        if (!expectedSignature.equals(parts[2])) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        Map<String, Object> claims;
        try {
            claims = objectMapper.readValue(base64UrlDecode(parts[1]), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }

        long exp;
        try {
            exp = Long.parseLong(String.valueOf(claims.get("exp")));
        } catch (Exception e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }

        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (now > exp) {
            throw new TokenExpiredException("만료된 토큰입니다.");
        }

        return claims;
    }

    private void validateTokenType(Map<String, Object> claims, String expectedType) {
        String tokenType = String.valueOf(claims.get(TOKEN_TYPE_CLAIM));
        if (!expectedType.equals(tokenType)) {
            throw new InvalidTokenException("토큰 타입이 올바르지 않습니다.");
        }
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 직렬화에 실패했습니다.", e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM));
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 서명에 실패했습니다.", e);
        }
    }

    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlDecode(String input) {
        return new String(Base64.getUrlDecoder().decode(input), StandardCharsets.UTF_8);
    }

    public record JwtTokenPair(String accessToken, String refreshToken, LocalDateTime refreshExpiresAt) {
    }

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
