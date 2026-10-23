package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveDeptCommand
 * @Description 新增或编辑部门
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SaveDeptCommand {

    private String id;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private String parentId;

    private String deptDesc;
}
