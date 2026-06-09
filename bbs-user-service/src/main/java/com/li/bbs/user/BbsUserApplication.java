package com.li.bbs.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS用户服务启动类
 * 提供用户信息管理、个人资料维护、用户状态管理等功能
 *
 * @author li
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.li.bbs.user.repository")
public class BbsUserApplication {
    /**
     * 用户服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsUserApplication.class, args);
    }
}

