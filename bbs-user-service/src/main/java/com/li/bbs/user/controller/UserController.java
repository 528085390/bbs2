package com.li.bbs.user.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.common.UserContext;
import com.li.bbs.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable("id") Long id) {
        return ApiResponse.ok(userService.getProfile(id));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = UserContext.getUserId(request);
        if (userId == null) {
            return new ApiResponse<>(401, "请先登录", null);
        }
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProfile(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> profile) {
        try {
            return ApiResponse.ok(userService.updateProfile(id, profile));
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

