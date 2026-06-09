package com.li.bbs.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS网关服务启动类
 * 微服务架构统一入口，负责路由转发、权限校验、流量控制、限流熔断等功能
 *
 * @author li
 * @since 1.0.0
 */
@SpringBootApplication
public class BbsGatewayApplication {
    /**
     * 网关服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsGatewayApplication.class, args);
    }
}

