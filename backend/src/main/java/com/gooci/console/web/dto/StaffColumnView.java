package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffColumnView
 * @Description 员工列表列配置一项
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffColumnView {

    private String colKey;
    private String colName;
    private String visibleFlag;
}
