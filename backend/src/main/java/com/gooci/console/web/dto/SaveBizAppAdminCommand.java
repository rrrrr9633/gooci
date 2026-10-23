package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveBizAppAdminCommand
 * @Description 给已植入模块指定管理员（写入该模块功能角色）
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class SaveBizAppAdminCommand {

    @NotBlank(message = "请选择模块")
    private String appCode;

    @NotBlank(message = "请选择人员")
    private String userId;

    @NotBlank(message = "请选择角色")
    private String roleName;
}
