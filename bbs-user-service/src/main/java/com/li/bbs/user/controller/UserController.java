package com.li.bbs.user.controller;

import com.li.bbs.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable Long id) {
        return ApiResponse.ok(users.getOrDefault(id, Map.of("id", id, "displayName", "user-" + id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, Object> profile) {
        users.put(id, profile);
        return ApiResponse.ok(profile);
    }
}

