package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @ClassName InboxRecord
 * @Description 站内信。与工作台「消息动态」同一数据，不做自动推送
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
@TableName("TM_INBOX")
public class InboxRecord extends BaseEntity {

    @TableField("APP_CODE")
    private String appCode;

    @TableField("APP_NAME")
    private String appName;

    @TableField("CONTENT")
    private String content;

    @TableField("PUBLISHER")
    private String publisher;

    @TableField("SEND_TIME")
    private LocalDateTime sendTime;

    @TableField("READ_FLAG")
    private String readFlag;

    @TableField("TM_SYS_USER_ID")
    private String tmSysUserId;
}
