package com.li.bbs.permission.controller;

import com.li.bbs.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @GetMapping("/roles")
    public ApiResponse<List<String>> roles() {
        return ApiResponse.ok(List.of("ROLE_USER", "ROLE_MOD", "ROLE_ADMIN"));
    }

    @PostMapping("/check")
    public ApiResponse<Boolean> check(@RequestBody Map<String, String> req) {
        String role = req.getOrDefault("role", "ROLE_USER");
        String action = req.getOrDefault("action", "read");
        boolean allowed = "read".equals(action) || "ROLE_ADMIN".equals(role) || "ROLE_MOD".equals(role);
        return ApiResponse.ok(allowed);
    }
}

