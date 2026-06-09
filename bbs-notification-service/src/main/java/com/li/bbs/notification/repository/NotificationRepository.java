package com.li.bbs.notification.repository;

import com.li.bbs.notification.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface NotificationRepository {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findAllByOrderByCreatedAtDesc();
    int insert(Notification notification);
}
