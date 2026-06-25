package com.example.gradescopespringboot;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.GlobalExceptionHandler;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试
 * 直接验证 GlobalExceptionHandler 各方法的返回值结构
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException_ReturnsStructuredResult() {
        BusinessException ex = new BusinessException(409, "Username already exists");

        Result<Void> result = handler.handleBusinessException(ex);

        assertNotNull(result);
        assertEquals(409, result.getCode());
        assertEquals("Username already exists", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testHandleMethodArgumentNotValid_Returns400WithFieldErrors() {
        // 构造一个模拟的 MethodArgumentNotValidException
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "username", "must not be blank"));
        bindingResult.addError(new FieldError("target", "password", "must be at least 8 characters"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        Result<Void> result = handler.handleMethodArgumentNotValid(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("must not be blank"));
        assertTrue(result.getMessage().contains("must be at least 8 characters"));
    }

    @Test
    void testHandleIllegalArgument_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");

        Result<Void> result = handler.handleIllegalArgument(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("Invalid parameter", result.getMessage());
    }

    @Test
    void testHandleException_Returns500WithoutSensitiveInfo() {
        Exception ex = new RuntimeException("Database connection failed");

        Result<Void> result = handler.handleException(ex);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("Internal server error", result.getMessage());
        assertNull(result.getData());
        // 确保原始异常消息不会泄露到客户端
        assertFalse(result.getMessage().contains("Database"));
    }

    @Test
    void testHandleException_WithNullMessage_Returns500() {
        Exception ex = new RuntimeException();

        Result<Void> result = handler.handleException(ex);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("Internal server error", result.getMessage());
    }
}
