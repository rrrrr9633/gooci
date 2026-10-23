package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppAuthBoardView
 * @Description 账号授权页。查询即按手机号或姓名筛选
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppAuthBoardView {

    private List<AppCatalogView> apps = new ArrayList<>();

    private SliceView<AppAuthItemView> slice = new SliceView<>();
}
