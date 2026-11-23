package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AppRole
 * @Description 已开通 APP 的功能角色或数据角色目录
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
@TableName("TM_APP_ROLE")
public class AppRole extends BaseEntity {

    @TableField("APP_CODE")
    private String appCode;

    @TableField("ROLE_KIND")
    private String roleKind;

    @TableField("ROLE_NAME")
    private String roleName;

    @TableField("SORT_NO")
    private Integer sortNo;
}
