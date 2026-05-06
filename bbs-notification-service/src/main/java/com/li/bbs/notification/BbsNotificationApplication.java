package com.li.bbs.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BbsNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbsNotificationApplication.class, args);
    }
}

