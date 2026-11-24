package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AiHubView
 * @Description AI 中台绑定。浏览器只拿公开的 harness 入口，不拿内部下一跳
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class AiHubView {

    private String appCode;

    private String runtime;

    private String profile;

    private String framePath;

    private String launchUrl;

    private boolean bound;

    private String deny;

    private List<AiHubCatalogItem> catalog = new ArrayList<>();
}
