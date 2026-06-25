package com.example.gradescopespringboot.common.exception;

/**
 * 未授权异常，当用户未登录或登录状态已过期时抛出。
 * 对应 HTTP 状态码 401。
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
