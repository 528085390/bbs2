package com.li.bbs.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BbsSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbsSearchApplication.class, args);
    }
}
