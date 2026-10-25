package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName BizChangeView
 * @Description 工商登记变更表
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class BizChangeView {

    private String enterName;
    private String creditCode;
    private String licenseType;
    private String licenseScan;
    private String legalPerson;
    private String legalIdFront;
    private String legalIdBack;
    private String changeProof;
    private String bizRemark;
}
