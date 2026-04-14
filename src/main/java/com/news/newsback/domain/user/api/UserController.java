package com.news.newsback.domain.user.api;

import com.news.newsback.domain.user.application.UserService;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 및 사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "일반 회원가입", description = "이메일과 비밀번호를 사용하여 새로운 계정을 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(
            @Valid @RequestBody UserRequest.Signup request) {

        User savedUser = userService.signup(request.getEmail(), request.getPassword());
        UserResponse response = UserResponse.from(savedUser);

        return ResponseEntity
            .status(201)
            .body(ApiResponse.success(response));
    }

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserRequest.Login request) {
        AuthResponse response = userService.login(request.getEmail(), request.getPassword(), request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "소셜 로그인", description = "카카오 또는 구글 액세스 토큰을 사용하여 로그인하거나 회원가입합니다.")
    @PostMapping("/social/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(
            @Valid @RequestBody UserRequest.SocialLogin request, @PathVariable String provider) {
        AuthResponse response = userService.socialLogin(provider, request.getSocialToken(), request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 재발급", description = "유효한 refresh token으로 access/refresh 토큰을 재발급합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody UserRequest.Refresh request) {
        AuthResponse response = userService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하고 로그아웃", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody UserRequest.Logout request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success());
    }


}
