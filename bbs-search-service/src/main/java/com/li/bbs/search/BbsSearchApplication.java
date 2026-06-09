package com.li.bbs.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BBS搜索服务启动类
 * 提供全站内容搜索、搜索建议、搜索历史管理等功能
 * 依赖帖子服务获取帖子数据，支持关键词高亮、分页搜索
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.search.repository")
@EnableFeignClients
@SpringBootApplication
public class BbsSearchApplication {
    /**
     * 搜索服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsSearchApplication.class, args);
    }
}
