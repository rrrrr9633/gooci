package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OpsChangeRowView
 * @Description 待厂商处理的工商/银行申请
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class OpsChangeRowView {

    private String enterId;

    private String enterName;

    private String kind;

    private String remark;
}
