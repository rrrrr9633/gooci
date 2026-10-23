package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffFieldView
 * @Description 员工自定义字段
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffFieldView {

    private String fieldId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private String requiredFlag;
    private String hintText;
    private Integer sortNo;
}
