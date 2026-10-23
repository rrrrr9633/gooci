package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ConsoleOverview
 * @Description 工作台首页数字与消息动态
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class ConsoleOverview {

    private Integer unreadMessageCount;
    private Integer pendingTaskCount;
    private Integer linkedDeviceCount;
    private Integer onlineDeviceCount;
    private List<InboxTickerView> recentMessages = new ArrayList<>();
}
