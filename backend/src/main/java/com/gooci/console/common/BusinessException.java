package com.gooci.console.common;

/**
 * @ClassName BusinessException
 * @Description 业务异常
 * @author GooCi
 * @date 2026-03-23
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
