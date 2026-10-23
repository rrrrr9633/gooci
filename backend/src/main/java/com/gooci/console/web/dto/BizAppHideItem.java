package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName BizAppHideItem
 * @Description 一块入口是否隐藏
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class BizAppHideItem {

    @NotBlank(message = "请选择模块")
    private String appCode;

    @NotBlank(message = "请选择是否隐藏")
    private String hideFlag;
}
