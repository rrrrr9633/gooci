package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveDeviceCommand
 * @Description 给部门分配/新增设备
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SaveDeviceCommand {

    @NotBlank(message = "部门不能为空")
    private String deptId;

    @NotBlank(message = "设备序列号不能为空")
    private String deviceSn;

    private String deviceAlias;
    private String boxCode;
    private String brandName;
    private String modelName;
    private Integer appOpenCount;
}
