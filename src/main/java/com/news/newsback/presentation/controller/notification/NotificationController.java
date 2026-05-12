package com.news.newsback.presentation.controller.notification;

import com.news.newsback.application.notification.FcmTokenService;
import com.news.newsback.application.notification.NotificationResponse;
import com.news.newsback.application.notification.NotificationService;
import com.news.newsback.application.notification.PushNotificationService;
import com.news.newsback.global.common.CommonReponse;
import com.news.newsback.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "인앱 알림 내역 API")
public class NotificationController {

    private final FcmTokenService fcmTokenService;
    private final PushNotificationService pushNotificationService;
    private final NotificationService notificationService;

    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = "웹 클라이언트에서 발급받은 FCM token을 Authorization 헤더의 사용자 기준으로 등록하거나 갱신합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "FCM 토큰 등록/갱신 성공")
    @PostMapping("/fcm-token")
    public CommonReponse<Void> registerFcmToken(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody NotificationRequest.FcmToken request
    ) {
        fcmTokenService.registerToken(user.userId(), request.getToken());
        return CommonReponse.success(null);
    }

    @Operation(
            summary = "FCM 토큰 비활성화",
            description = "로그아웃 또는 알림 권한 해제 시 Authorization 헤더의 사용자 기준으로 FCM token을 비활성화합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "FCM 토큰 비활성화 성공")
    @DeleteMapping("/fcm-token")
    public CommonReponse<Void> disableFcmToken(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody NotificationRequest.FcmToken request
    ) {
        fcmTokenService.disableToken(user.userId(), request.getToken());
        return CommonReponse.success(null);
    }

    @Operation(summary = "테스트 푸시 발송", description = "특정 사용자에게 FCM 테스트 푸시를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "테스트 푸시 발송 요청 성공")
    @PostMapping("/test-push")
    public CommonReponse<Void> sendTestPush(@Valid @RequestBody NotificationRequest.TestPush request) {
        pushNotificationService.sendToUser(request.getUserId(), request.getTitle(), request.getBody());
        return CommonReponse.success(null);
    }

    @Operation(
            summary = "알림 내역 조회",
            description = "인증된 사용자의 키워드 뉴스 알림 내역을 최신순으로 조회합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "알림 내역 조회 성공")
    @GetMapping
    public CommonReponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "페이지 번호, 0부터 시작", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return CommonReponse.success(notificationService.getNotifications(user.userId(), PageRequest.of(page, size)));
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "인증된 사용자의 특정 알림을 읽음 처리합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    @PatchMapping("/{notificationId}/read")
    public CommonReponse<Void> markAsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(user.userId(), notificationId);
        return CommonReponse.success();
    }

    @Operation(
            summary = "전체 알림 읽음 처리",
            description = "인증된 사용자의 미읽음 알림을 모두 읽음 처리합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponse(responseCode = "200", description = "전체 알림 읽음 처리 성공")
    @PatchMapping("/read-all")
    public CommonReponse<Void> markAllAsRead(@AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.markAllAsRead(user.userId());
        return CommonReponse.success();
    }
}
