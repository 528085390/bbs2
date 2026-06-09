package com.li.bbs.notification.consumer;

import com.li.bbs.common.mq.NotificationEvent;
import com.li.bbs.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "bbs.notification.queue")
    public void handleNotification(NotificationEvent event) {
        notificationService.push(event.userId(), event.type(), event.payload());
    }
}
