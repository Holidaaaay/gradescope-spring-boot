package com.example.gradescopespringboot.common.result;

import lombok.Data;

@Data
public class Result<T> {

    /**
     * Business status code
     */
    private Integer code;

    /**
     * Response message
     */
    private String message;

    /**
     * Response data
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}