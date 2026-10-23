package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName CertView
 * @Description 实名认证页
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class CertView {

    private String registerTime;
    private String bizPassed;
    private String bankPassed;
    private String certEnterName;
}
