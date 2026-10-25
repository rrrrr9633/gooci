package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeptRecord
 * @Description 企业部门
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TM_DEPT")
public class DeptRecord extends BaseEntity {

    @TableField("DEPT_CODE")
    private String deptCode;

    @TableField("DEPT_NAME")
    private String deptName;

    @TableField("PARENT_ID")
    private String parentId;

    @TableField("DEPT_DESC")
    private String deptDesc;

    @TableField("ENABLE_FLAG")
    private String enableFlag;

    @TableField("SORT_NO")
    private Integer sortNo;
}
