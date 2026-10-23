package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OpsUser
 * @Description 厂商运营账号，不进企业用户表
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
@TableName("TM_OPS_USER")
public class OpsUser extends BaseEntity {

    @TableField("LOGIN_NAME")
    private String loginName;

    @TableField("LOGIN_PASSWORD")
    private String loginPassword;

    @TableField("DISPLAY_NAME")
    private String displayName;
}
