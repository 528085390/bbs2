package com.li.bbs.post;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BBS帖子服务启动类
 * 提供帖子的发布、编辑、删除、查询、置顶、精华等核心功能
 * 依赖板块服务获取板块信息，支持Feign远程调用
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.post.repository")
@EnableFeignClients
@SpringBootApplication
public class BbsPostApplication {
    /**
     * 帖子服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsPostApplication.class, args);
    }
}
