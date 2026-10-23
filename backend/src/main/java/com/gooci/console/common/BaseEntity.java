package com.gooci.console.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @ClassName BaseEntity
 * @Description 业务表保留字段
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public abstract class BaseEntity {

    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(value = "CRT_USR_ID", fill = FieldFill.INSERT)
    private String crtUsrId;

    @TableField(value = "CRT_USR_NAME", fill = FieldFill.INSERT)
    private String crtUsrName;

    @TableField(value = "CRT_TIME", fill = FieldFill.INSERT)
    private LocalDateTime crtTime;

    @TableField(value = "UPD_USR_ID", fill = FieldFill.INSERT_UPDATE)
    private String updUsrId;

    @TableField(value = "UPD_USR_NAME", fill = FieldFill.INSERT_UPDATE)
    private String updUsrName;

    @TableField(value = "UPD_TIME", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updTime;

    @TableField("TS_ENTER_ID")
    private String tsEnterId;

    @TableLogic
    @TableField("IS_DELETE")
    private String deleted;
}
