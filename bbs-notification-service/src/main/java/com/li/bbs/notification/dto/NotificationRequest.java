package com.li.bbs.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Long userId,
        @NotBlank String type,
        String payload
) {
}

