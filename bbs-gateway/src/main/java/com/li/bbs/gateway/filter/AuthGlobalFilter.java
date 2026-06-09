package com.li.bbs.gateway.filter;

import com.li.bbs.common.JwtSupport;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 网关全局认证过滤器
 * 解析请求中的 Bearer Token，提取 userId 和 username，
 * 并将其注入到下游微服务的请求头中（X-User-Id、X-Username）
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** 无需认证即可访问的路径前缀 */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/",
            "/api/sections"
    );

    @Value("${app.jwt.secret:change_this_secret_to_a_long_random_value_at_least_32_chars}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 公开路径无需认证，直接放行
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization 头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 无 Token，放行（由下游微服务自行决定是否需要认证）
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            // 解析 Token
            Jws<Claims> claims = JwtSupport.parseToken(token, jwtSecret);
            Long userId = claims.getBody().get("userId", Long.class);
            String username = claims.getBody().getSubject();

            if (userId == null) {
                log.warn("Token 中缺少 userId, path={}", path);
                return chain.filter(exchange);
            }

            // 将用户信息注入请求头，转发给下游微服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();

            log.debug("认证成功: userId={}, username={}, path={}", userId, username, path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.warn("Token 解析失败: {}, path={}", e.getMessage(), path);
            return unauthorizedResponse(exchange, "无效的认证令牌");
        }
    }

    @Override
    public int getOrder() {
        // 设置为较高优先级，确保在其他过滤器之前执行
        return -100;
    }

    /**
     * 判断是否为公开路径（无需认证）
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
