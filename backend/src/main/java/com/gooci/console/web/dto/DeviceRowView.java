package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceRowView
 * @Description 部门设备一行
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class DeviceRowView {

    private String id;
    private String deviceSn;
    private String deviceAlias;
    private String boxCode;
    private String brandName;
    private String modelName;
    private Integer appOpenCount;
}
