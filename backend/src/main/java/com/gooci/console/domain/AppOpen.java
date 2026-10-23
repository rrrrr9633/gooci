package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AppOpen
 * @Description 企业已开通（已植入）工业 APP，不是销售目录
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
@TableName("TM_APP_OPEN")
public class AppOpen extends BaseEntity {

    @TableField("APP_CODE")
    private String appCode;

    @TableField("APP_NAME")
    private String appName;

    @TableField("SORT_NO")
    private Integer sortNo;

    @TableField("HIDE_FLAG")
    private String hideFlag;

    @TableField("ENTRY_PATH")
    private String entryPath;

    @TableField("TARGET_URL")
    private String targetUrl;
}
