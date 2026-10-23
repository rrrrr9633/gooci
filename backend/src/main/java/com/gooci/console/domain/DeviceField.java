package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceField
 * @Description 设备自定义字段
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
@TableName("TM_DEVICE_FIELD")
public class DeviceField extends BaseEntity {

    @TableField("FIELD_CODE")
    private String fieldCode;

    @TableField("FIELD_NAME")
    private String fieldName;

    @TableField("FIELD_TYPE")
    private String fieldType;

    @TableField("REQUIRED_FLAG")
    private String requiredFlag;

    @TableField("HINT_TEXT")
    private String hintText;

    @TableField("SORT_NO")
    private Integer sortNo;
}
