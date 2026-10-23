package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName RemoveStaffCommand
 * @Description 移除管理员或部门成员
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class RemoveStaffCommand {

    @NotBlank(message = "用户不能为空")
    private String userId;

    private String deptId;
}
