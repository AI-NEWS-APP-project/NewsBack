package com.news.newsback.infra.fcm;

import java.util.List;

public interface FcmClient {

    List<FcmSendResult> sendToTokens(List<String> tokens, PushMessage message);
}
