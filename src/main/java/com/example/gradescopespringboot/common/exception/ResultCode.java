package com.example.gradescopespringboot.common.exception;

import lombok.Getter;

/**
 * 统一响应状态码枚举。
 * 所有业务错误码在此集中定义，避免魔法数字散落各处。
 */
@Getter
public enum ResultCode {

    /**
     * 操作成功
     */
    SUCCESS(200, "success"),

    /**
     * 请求参数错误 / 校验失败
     */
    BAD_REQUEST(400, "Bad request"),

    /**
     * 未认证，JWT 缺失或无效
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * 禁止访问，权限不足
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "Resource not found"),

    /**
     * 资源冲突，如唯一键冲突
     */
    CONFLICT(409, "Conflict"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "Internal server error");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
