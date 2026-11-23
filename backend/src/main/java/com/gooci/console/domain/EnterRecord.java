package com.gooci.console.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gooci.console.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @ClassName EnterRecord
 * @Description 企业主数据
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
@TableName("TM_ENTER")
public class EnterRecord extends BaseEntity {

    @TableField("ENTER_CODE")
    private String enterCode;

    @TableField("ENTER_NAME")
    private String enterName;

    @TableField("CERT_STATUS")
    private String certStatus;

    @TableField("REGION_TEXT")
    private String regionText;

    @TableField("ENTER_NATURE")
    private String enterNature;

    @TableField("FOUND_DATE")
    private String foundDate;

    @TableField("ENTER_SCALE")
    private String enterScale;

    @TableField("INDUSTRY_NAME")
    private String industryName;

    @TableField("CONTACT_NAME")
    private String contactName;

    @TableField("CONTACT_MOBILE")
    private String contactMobile;

    @TableField("CONTACT_PHONE")
    private String contactPhone;

    @TableField("CONTACT_MAIL")
    private String contactMail;

    @TableField("ENTER_INTRO")
    private String enterIntro;

    @TableField("PROVINCE_NAME")
    private String provinceName;

    @TableField("CITY_NAME")
    private String cityName;

    @TableField("DISTRICT_NAME")
    private String districtName;

    @TableField("ADDRESS_TEXT")
    private String addressText;

    @TableField("REGISTER_TIME")
    private LocalDateTime registerTime;

    @TableField("BIZ_CERT_STATUS")
    private String bizCertStatus;

    @TableField("BANK_CERT_STATUS")
    private String bankCertStatus;

    @TableField("CERT_ENTER_NAME")
    private String certEnterName;

    @TableField("CREDIT_CODE")
    private String creditCode;

    @TableField("LICENSE_TYPE")
    private String licenseType;

    @TableField("LEGAL_PERSON")
    private String legalPerson;

    @TableField("LICENSE_SCAN")
    private String licenseScan;

    @TableField("LEGAL_ID_FRONT")
    private String legalIdFront;

    @TableField("LEGAL_ID_BACK")
    private String legalIdBack;

    @TableField("CHANGE_PROOF")
    private String changeProof;

    @TableField("BIZ_REMARK")
    private String bizRemark;

    @TableField("BANK_NAME")
    private String bankName;

    @TableField("BANK_PROVINCE")
    private String bankProvince;

    @TableField("BANK_CITY")
    private String bankCity;

    @TableField("BANK_BRANCH")
    private String bankBranch;

    @TableField("BANK_ACCOUNT")
    private String bankAccount;

    @TableField("BANK_PHONE")
    private String bankPhone;

    @TableField("BANK_REMARK")
    private String bankRemark;
}
