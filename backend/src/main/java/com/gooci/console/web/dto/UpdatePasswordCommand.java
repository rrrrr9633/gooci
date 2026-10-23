package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UpdatePasswordCommand
 * @Description 改登录密码
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class UpdatePasswordCommand {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
