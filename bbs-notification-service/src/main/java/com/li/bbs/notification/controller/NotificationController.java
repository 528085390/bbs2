package com.li.bbs.notification.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.notification.domain.Notification;
import com.li.bbs.notification.dto.NotificationRequest;
import com.li.bbs.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<Notification>> list(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(notificationService.list(userId));
    }

    @PostMapping("/system")
    public ApiResponse<Void> systemNotify(@Valid @RequestBody NotificationRequest request) {
        notificationService.push(request.userId(), request.type(), request.payload());
        return ApiResponse.ok();
    }

    @PostMapping("/internal/comment")
    public ApiResponse<Void> commentNotify(@RequestBody Map<String, Object> req) {
        notificationService.push(Long.parseLong(String.valueOf(req.get("userId"))), String.valueOf(req.get("type")), String.valueOf(req.get("payload")));
        return ApiResponse.ok();
    }

    @PostMapping("/internal/interaction")
    public ApiResponse<Void> interactionNotify(@RequestBody Map<String, Object> req) {
        notificationService.push(Long.parseLong(String.valueOf(req.get("userId"))), String.valueOf(req.get("type")), String.valueOf(req.get("payload")));
        return ApiResponse.ok();
    }
}
