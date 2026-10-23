package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AppGrantView
 * @Description 已开通应用一行
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class AppGrantView {

    private String appCode;
    private String appName;
    private String funcRoleName;
    private String dataRoleName;
}
