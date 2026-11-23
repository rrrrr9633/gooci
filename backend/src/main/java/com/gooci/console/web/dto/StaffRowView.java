package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffRowView
 * @Description 管理员或部门成员一行
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class StaffRowView {

    private String userId;
    private String mobile;
    private String displayName;
    private String mailAddr;
    private String deptName;
}
