package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.common.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName GlobalExceptionAdvice
 * @Description 统一异常出口
 * @author GooCi
 * @date 2026-03-23
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException ex) {
        return ApiResult.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "参数错误"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResult.fail(message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception ex) {
        return ApiResult.fail(ex.getMessage() == null ? "系统异常" : ex.getMessage());
    }
}
