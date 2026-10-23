package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffFieldVal
 * @Description 员工自定义字段值
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
@TableName("TM_STAFF_FIELD_VAL")
public class StaffFieldVal extends BaseEntity {

    @TableField("TM_SYS_USER_ID")
    private String tmSysUserId;

    @TableField("TM_STAFF_FIELD_ID")
    private String tmStaffFieldId;

    @TableField("FIELD_VALUE")
    private String fieldValue;
}
