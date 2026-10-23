package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AssignDeviceCommand
 * @Description 确认部门已分配设备列表
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class AssignDeviceCommand {

    @NotBlank(message = "部门不能为空")
    private String deptId;

    private List<String> deviceIds = new ArrayList<>();
}
