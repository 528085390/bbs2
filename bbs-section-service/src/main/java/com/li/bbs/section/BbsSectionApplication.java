package com.li.bbs.section;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BBS板块服务启动类
 * 提供论坛板块/分类的增删改查、板块排序、板块状态管理等功能
 *
 * @author li
 * @since 1.0.0
 */
@MapperScan("com.li.bbs.section.repository")
@SpringBootApplication
public class BbsSectionApplication {
    /**
     * 板块服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsSectionApplication.class, args);
    }
}
