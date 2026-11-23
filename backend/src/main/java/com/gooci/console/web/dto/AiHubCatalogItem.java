package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AiHubCatalogItem
 * @Description 中台内可选的一项 AI 功能。只有编码、名称和带 fn 的工作台路径
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class AiHubCatalogItem {

    private String appCode;

    private String appName;

    private String entryPath;
}
