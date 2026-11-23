package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UpdateEnterCommand
 * @Description 完善企业资料
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class UpdateEnterCommand {

    private String enterName;
    private String enterNature;
    private String foundDate;
    private String enterScale;
    private String industryName;
    private String contactName;
    private String contactMobile;
    private String contactPhone;
    private String contactMail;
    private String enterIntro;
    private String provinceName;
    private String cityName;
    private String districtName;
    private String addressText;
}
