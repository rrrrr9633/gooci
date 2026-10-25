package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppRoleBoardView
 * @Description 功能角色 / 数据角色页。查询即按已开通 APP 筛选
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppRoleBoardView {

    private List<AppOpenView> apps = new ArrayList<>();

    private SliceView<AppRoleRowView> slice = new SliceView<>();
}
