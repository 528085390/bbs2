package com.li.bbs.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS认证服务启动类
 * 提供用户登录、注册、身份认证、JWT令牌发放等核心认证功能
 *
 * @author li
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.li.bbs.auth.repository")
public class BbsAuthApplication {
    /**
     * 认证服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsAuthApplication.class, args);
    }
}

