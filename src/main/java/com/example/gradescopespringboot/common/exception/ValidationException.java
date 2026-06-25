package com.example.gradescopespringboot.common.exception;

/**
 * 参数校验异常，当请求参数不符合校验规则时抛出。
 * 对应 HTTP 状态码 400。
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ResultCode.BAD_REQUEST, message);
    }
}
