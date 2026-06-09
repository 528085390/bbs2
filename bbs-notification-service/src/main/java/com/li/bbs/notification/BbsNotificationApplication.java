package com.li.bbs.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS通知服务启动类
 * 提供系统通知、消息推送、站内信管理等功能
 * 通过RabbitMQ异步消费通知消息，保证消息可靠投递
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.notification.repository")
@SpringBootApplication
public class BbsNotificationApplication {
    /**
     * 通知服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsNotificationApplication.class, args);
    }
}
