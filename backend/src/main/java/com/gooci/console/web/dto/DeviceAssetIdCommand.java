package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceAssetIdCommand
 * @Description 设备台账主键
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceAssetIdCommand {

    @NotBlank(message = "设备不能为空")
    private String id;
}
