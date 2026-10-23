package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveDeviceAssetCommand
 * @Description 新增或编辑设备台账
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveDeviceAssetCommand {

    private String id;

    private String deviceCode;

    private String deviceSn;

    @NotBlank(message = "设备别名不能为空")
    private String deviceAlias;

    private String deviceType;

    private String makerName;

    private String brandName;

    private String modelName;

    private String installPlace;

    private String buyDate;

    private String deptId;

    private String deviceStatus;

    private String deviceLevel;

    private String ownerUserId;

    private String remarkText;

    private List<DeviceExtraView> extras = new ArrayList<>();
}
