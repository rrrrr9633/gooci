package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SmsCodeCommand
 * @Description 发送短信验证码
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SmsCodeCommand {

    @NotBlank(message = "手机号不能为空")
    private String mobile;
}
