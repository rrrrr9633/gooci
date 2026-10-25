package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SysMenu
 * @Description 控制台菜单（内核菜单，业务模块后续植入）
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TM_SYS_MENU")
public class SysMenu extends BaseEntity {

    @TableField("PARENT_ID")
    private String parentId;

    @TableField("MENU_CODE")
    private String menuCode;

    @TableField("MENU_NAME")
    private String menuName;

    @TableField("ROUTE_PATH")
    private String routePath;

    @TableField("ICON_CODE")
    private String iconCode;

    @TableField("SORT_NO")
    private Integer sortNo;
}
