package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SysUser
 * @Description 系统用户
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TM_SYS_USER")
public class SysUser extends BaseEntity {

    @TableField("LOGIN_NAME")
    private String loginName;

    @TableField("LOGIN_PASSWORD")
    private String loginPassword;

    @TableField("DISPLAY_NAME")
    private String displayName;

    @TableField("MOBILE")
    private String mobile;

    @TableField("MAIL_ADDR")
    private String mailAddr;

    @TableField("JOB_NAME")
    private String jobName;

    @TableField("ROLE_NAME")
    private String roleName;

    @TableField("STAFF_CODE")
    private String staffCode;

    @TableField("REMARK_TEXT")
    private String remarkText;

    @TableField("WECHAT_NO")
    private String wechatNo;

    @TableField("DING_NO")
    private String dingNo;

    @TableField("CARD_NO")
    private String cardNo;
}
