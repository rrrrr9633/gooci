package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OpsAppRowView
 * @Description 某企业已植入模块一行
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class OpsAppRowView {

    private String appCode;

    private String appName;

    private String hideFlag;

    private String entryPath;

    private String targetUrl;

    private String health;

    private String healthHint;
}
