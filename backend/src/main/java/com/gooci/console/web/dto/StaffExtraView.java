package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffExtraView
 * @Description 员工自定义字段取值
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffExtraView {

    private String fieldId;
    private String fieldName;
    private String fieldValue;
}
