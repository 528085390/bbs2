package com.li.bbs.common;

/**
 * 统一API响应结果封装类
 * 所有接口返回值统一使用该类封装，包含响应码、消息、业务数据三部分
 * 响应码约定：0=成功，-1=业务失败，其他编码可根据业务需求扩展
 *
 * @author li
 * @since 1.0.0
 * @param <T> 响应数据的泛型类型
 */
public record ApiResponse<T>(
    /**
     * 响应状态码
     * 0表示成功，-1表示业务失败，其他值为自定义错误码
     */
    int code,

    /**
     * 响应描述信息
     * 成功时默认为"ok"，失败时为具体错误信息
     */
    String message,

    /**
     * 响应业务数据
     * 成功时返回具体业务对象，失败时可为null
     */
    T data
) {

    /**
     * 创建带返回数据的成功响应
     *
     * @param data 业务返回数据
     * @return 封装后的成功响应对象
     * @param <T> 业务数据的泛型类型
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    /**
     * 创建无返回数据的成功响应
     *
     * @return 封装后的成功响应对象，无业务数据
     */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "ok", null);
    }

    /**
     * 创建带错误信息的失败响应
     *
     * @param message 具体错误描述信息
     * @return 封装后的失败响应对象
     */
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }
}

