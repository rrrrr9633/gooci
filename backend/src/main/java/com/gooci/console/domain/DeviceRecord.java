package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceRecord
 * @Description 部门下设备
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TM_DEVICE")
public class DeviceRecord extends BaseEntity {

    @TableField("DEVICE_SN")
    private String deviceSn;

    @TableField("DEVICE_ALIAS")
    private String deviceAlias;

    @TableField("BOX_CODE")
    private String boxCode;

    @TableField("BRAND_NAME")
    private String brandName;

    @TableField("MODEL_NAME")
    private String modelName;

    @TableField("APP_OPEN_COUNT")
    private Integer appOpenCount;

    @TableField("TM_DEPT_ID")
    private String tmDeptId;

    @TableField("DEVICE_CODE")
    private String deviceCode;

    @TableField("DEVICE_TYPE")
    private String deviceType;

    @TableField("MAKER_NAME")
    private String makerName;

    @TableField("INSTALL_PLACE")
    private String installPlace;

    @TableField("BUY_DATE")
    private String buyDate;

    @TableField("DEVICE_STATUS")
    private String deviceStatus;

    @TableField("DEVICE_LEVEL")
    private String deviceLevel;

    @TableField("OWNER_USER_ID")
    private String ownerUserId;

    @TableField("REMARK_TEXT")
    private String remarkText;

    @TableField("ONLINE_FLAG")
    private String onlineFlag;
}
