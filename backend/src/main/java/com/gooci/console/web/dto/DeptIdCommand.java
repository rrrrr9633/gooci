package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeptIdCommand
 * @Description 部门主键
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class DeptIdCommand {

    @NotBlank(message = "部门不能为空")
    private String deptId;
}
