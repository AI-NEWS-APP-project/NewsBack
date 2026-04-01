package com.news.newsback.domain.user.api;

import com.news.newsback.domain.user.application.UserService;
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

        throw new UnsupportedOperationException("구현 중");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody UserRequest.Login request) {

        throw new UnsupportedOperationException("구현 중");
    }

    @PostMapping("social/{provider}")
    public ResponseEntity<ApiResponse<UserResponse>> socialLogin(
            @Valid @RequestBody UserRequest.SocialLogin request, @PathVariable String provider) {

        throw new UnsupportedOperationException("구현 중");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody UserRequest.Logout request) {

        throw new UnsupportedOperationException("구현 중");
    }


}
