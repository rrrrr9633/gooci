package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceFieldVal
 * @Description 设备自定义字段值
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
@TableName("TM_DEVICE_FIELD_VAL")
public class DeviceFieldVal extends BaseEntity {

    @TableField("TM_DEVICE_ID")
    private String tmDeviceId;

    @TableField("TM_DEVICE_FIELD_ID")
    private String tmDeviceFieldId;

    @TableField("FIELD_VALUE")
    private String fieldValue;
}
