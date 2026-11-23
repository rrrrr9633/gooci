package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StaffItemView
 * @Description 员工管理列表一行
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffItemView {

    private String userId;
    private String staffCode;
    private String displayName;
    private String mobile;
    private String deptName;
    private List<String> deptIds = new ArrayList<>();
    private String mailAddr;
    private String remarkText;
    private String wechatNo;
    private String dingNo;
    private String cardNo;
    private String crtTime;
    private List<StaffExtraView> extras = new ArrayList<>();
}
