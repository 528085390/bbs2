package com.li.bbs.interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BbsInteractionApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbsInteractionApplication.class, args);
    }
}
