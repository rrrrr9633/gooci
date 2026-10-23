package com.gooci.console.common;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ApiResult
 * @Description 统一接口返回
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class ApiResult<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> result = new ApiResult<T>();
        result.setCode(0);
        result.setMessage("ok");
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> fail(String message) {
        ApiResult<T> result = new ApiResult<T>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }
}
