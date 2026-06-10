package com.li.bbs.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
public class DatabaseInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializerConfig.class);
    private static final Pattern DB_URL_PATTERN = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");

    @Bean
    public static BeanFactoryPostProcessor databaseCreator(Environment env) {
        return beanFactory -> {
            String url = env.getProperty("spring.datasource.url");
            if (url == null || url.isBlank()) {
                return;
            }
            Matcher matcher = DB_URL_PATTERN.matcher(url);
            if (!matcher.find()) {
                return;
            }
            String dbName = matcher.group(1);
            String baseUrl = "jdbc:mysql://" + url.substring("jdbc:mysql://".length(), url.indexOf("/", "jdbc:mysql://".length()));
            String adminUrl = baseUrl + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String username = env.getProperty("spring.datasource.username", "root");
            String password = env.getProperty("spring.datasource.password", "");

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    log.info("Database '{}' auto-created or already exists", dbName);
                }
            } catch (Exception e) {
                log.warn("Failed to auto-create database '{}': {}. Ensure the database exists or check MySQL permissions.", dbName, e.getMessage());
            }
        };
    }
}
