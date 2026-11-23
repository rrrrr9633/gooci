package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName BankChangeView
 * @Description 银行账户变更表
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class BankChangeView {

    private String bankName;
    private String bankProvince;
    private String bankCity;
    private String bankBranch;
    private String bankAccount;
    private String bankPhone;
    private String bankRemark;
    private String servicePhone;
}
