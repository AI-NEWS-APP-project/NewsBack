package com.news.newsback.infra.fcm;

public record FcmSendResult(
        String token,
        boolean success,
        String failureReason,
        boolean invalidToken
) {

    public static FcmSendResult success(String token) {
        return new FcmSendResult(token, true, null, false);
    }

    public static FcmSendResult failure(String token, String failureReason, boolean invalidToken) {
        return new FcmSendResult(token, false, failureReason, invalidToken);
    }
}
