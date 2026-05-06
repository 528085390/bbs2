package com.li.bbs.auth.controller;

import com.li.bbs.auth.dto.LoginRequest;
import com.li.bbs.auth.dto.LoginResponse;
import com.li.bbs.auth.dto.RegisterRequest;
import com.li.bbs.auth.service.AuthService;
import com.li.bbs.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("auth-up");
    }
}

