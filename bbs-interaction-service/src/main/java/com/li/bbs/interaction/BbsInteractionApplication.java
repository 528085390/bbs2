package com.li.bbs.interaction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BBS交互服务启动类
 * 提供用户互动功能：帖子点赞、收藏、用户关注、粉丝管理等
 * 依赖帖子服务校验帖子存在性，依赖通知服务发送互动提醒
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.interaction.repository")
@EnableFeignClients
@SpringBootApplication
public class BbsInteractionApplication {
    /**
     * 交互服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsInteractionApplication.class, args);
    }
}
