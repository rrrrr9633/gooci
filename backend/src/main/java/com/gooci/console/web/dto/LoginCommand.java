package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName LoginCommand
 * @Description 登录入参
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class LoginCommand {

    @NotBlank(message = "账号不能为空")
    private String loginName;

    @NotBlank(message = "密码不能为空")
    private String loginPassword;
}
