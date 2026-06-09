package com.li.bbs.common.mq;

public record NotificationEvent(
    Long userId,
    String type,
    String payload
) {}
