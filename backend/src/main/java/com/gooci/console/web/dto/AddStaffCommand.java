package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AddStaffCommand
 * @Description 添加企业管理员或部门成员
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class AddStaffCommand {

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @NotBlank(message = "姓名不能为空")
    private String displayName;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    private String mailAddr;

    private String deptId;
}
