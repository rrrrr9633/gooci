package com.gooci.console.support;

import com.gooci.console.domain.SysMenu;
import com.gooci.console.web.dto.MenuView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName MenuTreeSupport
 * @Description 菜单树组装
 * @author GooCi
 * @date 2026-03-23
 */
public final class MenuTreeSupport {

    private MenuTreeSupport() {
    }

    public static List<MenuView> toTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<MenuView>();
        }
        List<SysMenu> ordered = menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSortNo, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        Map<String, MenuView> index = ordered.stream().collect(Collectors.toMap(SysMenu::getId, MenuTreeSupport::toView));
        List<MenuView> roots = new ArrayList<MenuView>();
        for (SysMenu menu : ordered) {
            MenuView current = index.get(menu.getId());
            if (menu.getParentId() == null || menu.getParentId().isEmpty()) {
                roots.add(current);
            } else {
                MenuView parent = index.get(menu.getParentId());
                if (parent == null) {
                    roots.add(current);
                } else {
                    parent.getChildren().add(current);
                }
            }
        }
        return roots;
    }

    private static MenuView toView(SysMenu menu) {
        MenuView view = new MenuView();
        view.setId(menu.getId());
        view.setMenuCode(menu.getMenuCode());
        view.setMenuName(menu.getMenuName());
        view.setRoutePath(menu.getRoutePath());
        view.setIconCode(menu.getIconCode());
        return view;
    }
}
