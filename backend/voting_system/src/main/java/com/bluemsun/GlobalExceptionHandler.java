package com.bluemsun;

import com.bluemsun.entity.dto.ResultDto;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理请求体缺失或格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultDto<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        // 直接使用构造函数，第一个参数传false表示失败
        return new ResultDto<>(false, "请求参数格式错误或缺失: " + e.getLocalizedMessage(), null);
    }

    // 处理不支持的请求方法
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultDto<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String message = "不支持的请求方法: " + e.getMethod() +
                "，支持的方法有: " + String.join(",", e.getSupportedMethods());
        return new ResultDto<>(false, message, null);
    }

    // 处理其他所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public ResultDto<Object> handleAllUncaughtException(Exception e) {
        return new ResultDto<>(false, "服务器内部错误: " + e.getMessage(), null);
    }
}
