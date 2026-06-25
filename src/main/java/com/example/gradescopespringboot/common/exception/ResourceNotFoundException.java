package com.example.gradescopespringboot.common.exception;

/**
 * 资源未找到异常，当查询的目标资源不存在或已被逻辑删除时抛出。
 * 对应 HTTP 状态码 404。
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }
}
