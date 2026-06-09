package com.li.bbs.comment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BBS评论服务启动类
 * 提供帖子评论的发布、编辑、删除、查询、盖楼回复等功能
 * 依赖帖子服务校验帖子存在性，依赖通知服务发送评论提醒
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.comment.repository")
@EnableFeignClients
@SpringBootApplication
public class BbsCommentApplication {
    /**
     * 评论服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsCommentApplication.class, args);
    }
}
