package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveAppRoleCommand
 * @Description 创建功能角色或数据角色
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class SaveAppRoleCommand {

    @NotBlank(message = "角色类型不能为空")
    private String kind;

    @NotBlank(message = "请选择已开通APP")
    private String appCode;

    @NotBlank(message = "请填写角色名称")
    private String roleName;
}
