package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AppRoleRowView
 * @Description 功能角色 / 数据角色列表一行
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppRoleRowView {

    private String id;

    private String appCode;

    private String appName;

    private String roleName;
}
