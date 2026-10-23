package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceQuery
 * @Description 设备列表筛选
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceQuery {

    private String keyword;
    private String deptIds;
    private String deviceCode;
    private String deviceSn;
    private String deviceAlias;
    private String deviceType;
    private String makerName;
    private String brandName;
    private String modelName;
    private String installPlace;
    private String onlineFlag;
    private String deviceStatus;
    private String deviceLevel;
    private String ownerName;
    private String sortKey;
    private String sortDir;
    private long pageNo = 1;
    private long pageSize = 10;
}
