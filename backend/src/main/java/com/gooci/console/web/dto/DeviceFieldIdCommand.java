package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceFieldIdCommand
 * @Description 设备自定义字段主键
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceFieldIdCommand {

    @NotBlank(message = "字段不能为空")
    private String fieldId;
}
