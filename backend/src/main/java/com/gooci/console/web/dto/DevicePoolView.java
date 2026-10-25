package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DevicePoolView
 * @Description 部门分配设备：待分配 / 已分配
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DevicePoolView {

    private List<DeviceRowView> pending = new ArrayList<>();
    private List<DeviceRowView> assigned = new ArrayList<>();
}
