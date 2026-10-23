package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceExtraView
 * @Description 设备自定义字段取值
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceExtraView {

    private String fieldId;
    private String fieldName;
    private String fieldValue;
}
