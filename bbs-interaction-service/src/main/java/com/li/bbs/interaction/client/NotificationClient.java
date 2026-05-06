package com.li.bbs.interaction.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "bbs-notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/internal/interaction")
    ApiResponse<Void> sendInteractionNotification(@RequestBody Map<String, Object> payload);
}

