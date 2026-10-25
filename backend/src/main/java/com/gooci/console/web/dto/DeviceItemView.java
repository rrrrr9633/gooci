package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DeviceItemView
 * @Description 设备管理列表一行
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceItemView {

    private String id;
    private String deviceCode;
    private String deviceSn;
    private String deviceAlias;
    private String deviceType;
    private String makerName;
    private String brandName;
    private String modelName;
    private String installPlace;
    private String buyDate;
    private String deptId;
    private String deptName;
    private String onlineFlag;
    private String onlineText;
    private String deviceStatus;
    private String deviceLevel;
    private String ownerUserId;
    private String ownerName;
    private String ownerMobile;
    private String remarkText;
    private String crtTime;
    private List<DeviceExtraView> extras = new ArrayList<>();
}
