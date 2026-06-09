package com.li.bbs.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 从请求头中获取网关注入的用户信息
 *
 * 网关 AuthGlobalFilter 会将 Token 中的 userId 和 username
 * 注入到请求头 X-User-Id 和 X-Username 中
 */
public final class UserContext {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";

    private UserContext() {
    }

    /**
     * 从请求头获取当前登录用户的 ID
     *
     * @param request HTTP 请求
     * @return 用户ID，未登录时返回 null
     */
    public static Long getUserId(HttpServletRequest request) {
        String header = request.getHeader(HEADER_USER_ID);
        if (header != null && !header.isBlank()) {
            try {
                return Long.parseLong(header);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从请求头获取当前登录用户的用户名
     *
     * @param request HTTP 请求
     * @return 用户名，未登录时返回 null
     */
    public static String getUsername(HttpServletRequest request) {
        String header = request.getHeader(HEADER_USERNAME);
        return (header != null && !header.isBlank()) ? header : null;
    }
}
