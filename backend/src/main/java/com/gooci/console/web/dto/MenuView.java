package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MenuView
 * @Description 控制台菜单树
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class MenuView {

    private String id;
    private String menuCode;
    private String menuName;
    private String routePath;
    private String iconCode;
    private List<MenuView> children = new ArrayList<MenuView>();
}
