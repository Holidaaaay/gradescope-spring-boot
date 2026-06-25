package com.example.gradescopespringboot.common.exception;

import lombok.Getter;

/**
 * 业务异常基类，所有业务相关异常均继承此类。
 * 异常信息会被 {@link com.example.gradescopespringboot.common.exception.GlobalExceptionHandler}
 * 捕获并封装为统一的 {@link com.example.gradescopespringboot.common.result.Result} 响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码，对应 {@link ResultCode} 中的定义
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
