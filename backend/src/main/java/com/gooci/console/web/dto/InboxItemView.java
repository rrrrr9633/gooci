package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName InboxItemView
 * @Description 站内信列表行
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class InboxItemView {

    private String id;

    private String content;

    private String sendTime;

    private String readFlag;

    private String publisher;

    private String appCode;

    private String appName;
}
