package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UserAppGrant
 * @Description 用户已开通应用授权（植入后才有行，不预装目录模块）
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TR_USER_APP_GRANT")
public class UserAppGrant extends BaseEntity {

    @TableField("TM_SYS_USER_ID")
    private String tmSysUserId;

    @TableField("APP_CODE")
    private String appCode;

    @TableField("APP_NAME")
    private String appName;

    @TableField("FUNC_ROLE_NAME")
    private String funcRoleName;

    @TableField("DATA_ROLE_NAME")
    private String dataRoleName;
}
