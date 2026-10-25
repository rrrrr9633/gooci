package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveBizChangeCommand
 * @Description 提交工商登记变更申请
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SaveBizChangeCommand {

    @NotBlank(message = "企业名称不能为空")
    private String enterName;

    @NotBlank(message = "统一社会信用代码不能为空")
    private String creditCode;

    @NotBlank(message = "请选择企业证件类型")
    private String licenseType;

    @NotBlank(message = "请上传证件扫描件")
    private String licenseScan;

    private String legalPerson;

    private String legalIdFront;

    private String legalIdBack;

    @NotBlank(message = "请上传变更凭证")
    private String changeProof;

    private String bizRemark;
}
