package com.li.bbs.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BbsCommentApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbsCommentApplication.class, args);
    }
}
