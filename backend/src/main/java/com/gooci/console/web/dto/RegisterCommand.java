package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName RegisterCommand
 * @Description 企业/个人注册
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class RegisterCommand {

    @NotBlank(message = "注册类型不能为空")
    private String registerKind;

    @NotBlank(message = "账号不能为空")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    @NotBlank(message = "姓名不能为空")
    private String displayName;

    @NotBlank(message = "密码不能为空")
    private String loginPassword;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    private String enterName;

    private String jobName;

    private String inviteCode;

    private Boolean agreed;
}
