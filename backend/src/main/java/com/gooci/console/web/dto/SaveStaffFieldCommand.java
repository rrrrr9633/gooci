package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveStaffFieldCommand
 * @Description 保存员工自定义字段
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveStaffFieldCommand {

    private String fieldId;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    private String fieldType;

    private String requiredFlag;

    private String hintText;
}
