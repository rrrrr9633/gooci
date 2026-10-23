package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SysParameter
 * @Description 系统参数
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TS_SYS_PARAMETER")
public class SysParameter extends BaseEntity {

    @TableField("PARAM_CODE")
    private String paramCode;

    @TableField("PARAM_VALUE")
    private String paramValue;

    @TableField("PARAM_NAME")
    private String paramName;
}
