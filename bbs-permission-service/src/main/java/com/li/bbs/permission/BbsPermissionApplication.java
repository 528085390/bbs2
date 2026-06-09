package com.li.bbs.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS权限服务启动类
 * 提供用户权限管理、角色管理、访问控制、权限校验等功能
 *
 * @author li
 * @since 1.0.0
 */
@SpringBootApplication
public class BbsPermissionApplication {
    /**
     * 权限服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsPermissionApplication.class, args);
    }
}

