package com.li.bbs.notification.service;

import com.li.bbs.notification.domain.Notification;
import com.li.bbs.notification.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> list(Long userId) {
        if (userId == null) {
            return notificationRepository.findAllByOrderByCreatedAtDesc();
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Async
    @Transactional
    public void push(Long userId, String type, String payload) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setPayload(payload);
        notificationRepository.save(notification);
    }
}

