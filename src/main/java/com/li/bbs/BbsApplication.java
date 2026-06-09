package com.li.bbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * BBS论坛系统主启动类
 * 单体部署时的入口类，扫描所有Spring Bean和MyBatis Mapper接口
 *
 * @author li
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.li.bbs.repository")
public class BbsApplication {

    /**
     * 系统启动入口方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BbsApplication.class, args);
    }

}
