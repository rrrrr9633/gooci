package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeviceCodeRule
 * @Description 设备编号规则与列配置
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
@TableName("TM_DEVICE_CODE_RULE")
public class DeviceCodeRule extends BaseEntity {

    @TableField("CODE_NAME")
    private String codeName;

    @TableField("RULE_JSON")
    private String ruleJson;

    @TableField("COL_JSON")
    private String colJson;

    @TableField("SEQ_NO")
    private Integer seqNo;
}
