package com.li.bbs.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.li.bbs.common.ApiResponse;
import com.li.bbs.common.UserContext;
import com.li.bbs.user.domain.User;
import com.li.bbs.user.repository.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public UserController(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable("id") Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.ok(Map.of("id", id, "displayName", "user-" + id));
        }
        return ApiResponse.ok(buildResponse(user.getId(), user.getDisplayName(), user.getProfile()));
    }

    /**
     * 获取当前登录用户信息（从网关注入的请求头读取 userId）
     * 前端调用：GET /api/users/me
     * 需携带 Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = UserContext.getUserId(request);
        if (userId == null) {
            return new ApiResponse<>(401, "请先登录", null);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.ok(Map.of("id", userId, "displayName", "user-" + userId));
        }
        return ApiResponse.ok(buildResponse(user.getId(), user.getDisplayName(), user.getProfile()));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProfile(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> profile) {
        Map<String, Object> payload = profile == null ? Map.of() : profile;
        try {
            User existing = userMapper.selectById(id);
            String displayName = resolveDisplayName(payload, existing, id);
            String profileJson = objectMapper.writeValueAsString(payload);

            int updated = userMapper.updateProfile(id, displayName, profileJson);
            if (updated == 0) {
                User user = new User();
                user.setId(id);
                user.setDisplayName(displayName);
                user.setProfile(profileJson);
                userMapper.insert(user);
            }

            return ApiResponse.ok(buildResponse(id, displayName, profileJson));
        } catch (Exception e) {
            return ApiResponse.fail("failed to update profile: " + e.getMessage());
        }
    }

    private String resolveDisplayName(Map<String, Object> profile, User existing, Long id) {
        Object displayName = profile.get("displayName");
        if (displayName != null) {
            return String.valueOf(displayName);
        }
        if (existing != null && existing.getDisplayName() != null && !existing.getDisplayName().isBlank()) {
            return existing.getDisplayName();
        }
        return "user-" + id;
    }

    private Map<String, Object> buildResponse(Long id, String displayName, String profileJson) {
        Map<String, Object> profileMap;
        try {
            profileMap = objectMapper.readValue(profileJson == null ? "{}" : profileJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            profileMap = Map.of();
        }
        Map<String, Object> response = new HashMap<>(profileMap);
        response.put("id", id);
        response.put("displayName", displayName);
        return response;
    }
}

