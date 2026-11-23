package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DeviceBoardView
 * @Description 设备管理页
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceBoardView {

    private SliceView<DeviceItemView> slice = new SliceView<>();
    private List<DeviceColumnView> columns = new ArrayList<>();
    private List<DeviceFieldView> fields = new ArrayList<>();
    private List<DeptNodeView> depts = new ArrayList<>();
    private List<DeviceOwnerView> owners = new ArrayList<>();
    private List<String> deviceTypes = new ArrayList<>();
    private String nextCode;
}
