package com.li.bbs.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS文件服务启动类
 * 提供文件上传、下载、元数据管理、文件存储等功能
 * 支持多种存储策略：本地存储、云存储等
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.file.repository")
@SpringBootApplication
public class BbsFileApplication {
    /**
     * 文件服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsFileApplication.class, args);
    }
}
