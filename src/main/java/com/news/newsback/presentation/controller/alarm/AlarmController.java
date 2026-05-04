package com.news.newsback.presentation.controller.alarm;

import com.news.newsback.application.alarm.FcmTokenService;
import com.news.newsback.application.alarm.PushNotificationService;
import com.news.newsback.global.common.CommonReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Tag(name = "Alarm", description = "FCM 푸시 알림 API")
public class AlarmController {

    private final FcmTokenService fcmTokenService;
    private final PushNotificationService pushNotificationService;

    @Operation(summary = "FCM 토큰 등록/갱신", description = "웹 클라이언트에서 발급받은 FCM token을 등록하거나 갱신합니다.")
    @ApiResponse(responseCode = "200", description = "FCM 토큰 등록/갱신 성공")
    @PostMapping("/fcm-tokens")
    public CommonReponse<Void> registerFcmToken(@Valid @RequestBody AlarmRequest.FcmToken request) {
        fcmTokenService.registerToken(request.getUserId(), request.getToken());
        return CommonReponse.success(null);
    }

    @Operation(summary = "FCM 토큰 비활성화", description = "로그아웃 또는 알림 권한 해제 시 FCM token을 비활성화합니다.")
    @ApiResponse(responseCode = "200", description = "FCM 토큰 비활성화 성공")
    @DeleteMapping("/fcm-tokens")
    public CommonReponse<Void> disableFcmToken(@Valid @RequestBody AlarmRequest.FcmToken request) {
        fcmTokenService.disableToken(request.getUserId(), request.getToken());
        return CommonReponse.success(null);
    }

    @Operation(summary = "테스트 푸시 발송", description = "특정 사용자에게 FCM 테스트 푸시를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "테스트 푸시 발송 요청 성공")
    @PostMapping("/test-push")
    public CommonReponse<Void> sendTestPush(@Valid @RequestBody AlarmRequest.TestPush request) {
        pushNotificationService.sendToUser(request.getUserId(), request.getTitle(), request.getBody());
        return CommonReponse.success(null);
    }
}
