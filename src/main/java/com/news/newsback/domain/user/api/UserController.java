package com.news.newsback.domain.user.api;

import com.news.newsback.domain.user.application.UserService;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(
            @Valid @RequestBody UserRequest.Signup request) {

        User savedUser = userService.signup(request.getEmail(), request.getPassword());
        UserResponse response = UserResponse.from(savedUser);

        return ResponseEntity
            .status(201)
            .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserRequest.Login request) {
        AuthResponse response = userService.login(request.getEmail(), request.getPassword(), request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/social/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(
            @Valid @RequestBody UserRequest.SocialLogin request, @PathVariable String provider) {
        AuthResponse response = userService.socialLogin(provider, request.getSocialToken(), request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody UserRequest.Logout request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success());
    }


}
