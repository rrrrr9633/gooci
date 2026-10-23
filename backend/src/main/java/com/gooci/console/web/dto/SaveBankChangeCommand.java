package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveBankChangeCommand
 * @Description 提交银行账户变更申请
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SaveBankChangeCommand {

    @NotBlank(message = "请选择银行")
    private String bankName;

    @NotBlank(message = "请选择省份")
    private String bankProvince;

    @NotBlank(message = "请选择城市")
    private String bankCity;

    @NotBlank(message = "开户支行不能为空")
    private String bankBranch;

    @NotBlank(message = "企业对公账户不能为空")
    private String bankAccount;

    @NotBlank(message = "联系电话不能为空")
    private String bankPhone;

    private String bankRemark;
}
