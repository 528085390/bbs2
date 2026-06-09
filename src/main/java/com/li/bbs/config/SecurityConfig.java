package com.li.bbs.config;

import com.li.bbs.security.CustomUserDetailsService;
import com.li.bbs.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 单体部署时的安全配置，配置认证规则、权限控制、JWT过滤器等
 *
 * @author li
 * @since 1.0.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 自定义用户详情服务，用于加载用户信息
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * JWT认证过滤器，用于解析和验证请求中的JWT令牌
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数注入依赖
     *
     * @param userDetailsService 用户详情服务
     * @param jwtAuthenticationFilter JWT认证过滤器
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤链
     *
     * @param http HttpSecurity配置对象
     * @return 构建完成的SecurityFilterChain
     * @throws Exception 配置异常时抛出
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，因为使用JWT无状态认证
                .csrf().disable()
                // 禁用会话，使用无状态认证
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置请求授权规则
                .authorizeHttpRequests()
                // 认证接口、监控端点、H2控制台允许匿名访问
                .requestMatchers("/api/auth/**", "/actuator/**", "/h2-console/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated();

        // 添加JWT认证过滤器，在用户名密码认证过滤器之前执行
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 禁用X-Frame-Options，允许H2控制台使用iframe
        http.headers().frameOptions().disable();

        return http.build();
    }

    /**
     * 密码编码器Bean
     * 使用BCrypt算法进行密码加密，强度默认10
     *
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器Bean
     * 配置使用自定义用户详情服务和密码编码器
     *
     * @return AuthenticationManager实例
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

}

