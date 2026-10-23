package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName TodoRecord
 * @Description 待办任务条目。业务原理未定，当前只存列表字段
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
@TableName("TM_TODO")
public class TodoRecord extends BaseEntity {

    @TableField("BIZ_LINE")
    private String bizLine;

    @TableField("TASK_TYPE")
    private String taskType;

    @TableField("CONTENT")
    private String content;

    @TableField("SENDER_NAME")
    private String senderName;

    @TableField("HANDLE_PATH")
    private String handlePath;

    @TableField("STATUS_FLAG")
    private String statusFlag;

    @TableField("TM_SYS_USER_ID")
    private String tmSysUserId;
}
