package com.li.bbs.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT令牌解析工具类
 * 提供JWT令牌的解析和验证功能，基于JJWT库实现，使用HMAC-SHA签名算法
 * HMAC签名密钥需要满足算法要求的最小长度：HS256最少256位，HS512最少512位
 *
 * @author li
 * @since 1.0.0
 */
public final class JwtSupport {

    /**
     * 私有构造函数，禁止工具类实例化
     */
    private JwtSupport() {
    }

    /**
     * 解析并验证JWT令牌合法性
     *
     * @param token 待解析的JWT字符串，格式为header.payload.signature
     * @param secret HMAC签名密钥，长度需符合对应算法要求
     * @return 解析后的JWS声明对象，包含令牌的所有负载信息
     * @throws io.jsonwebtoken.MalformedJwtException 令牌格式错误时抛出
     * @throws io.jsonwebtoken.ExpiredJwtException 令牌已过期时抛出
     * @throws io.jsonwebtoken.SignatureException 签名验证失败时抛出
     * @throws IllegalArgumentException 密钥长度不符合要求时抛出
     */
    public static Jws<Claims> parseToken(String token, String secret) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}

