package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName RemoveDeviceCommand
 * @Description 从部门移除设备
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class RemoveDeviceCommand {

    @NotBlank(message = "设备不能为空")
    private String id;
}
