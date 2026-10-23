package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UserDeptRel
 * @Description 用户所属部门
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TR_USER_DEPT")
public class UserDeptRel extends BaseEntity {

    @TableField("TM_SYS_USER_ID")
    private String tmSysUserId;

    @TableField("TM_DEPT_ID")
    private String tmDeptId;
}
