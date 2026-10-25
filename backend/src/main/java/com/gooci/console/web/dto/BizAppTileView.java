package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName BizAppTileView
 * @Description 工业 APP 快捷入口一块。只有已植入且带入口路径的模块会出现
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class BizAppTileView {

    private String appCode;

    private String appName;

    private String entryPath;

    private String hideFlag;
}
