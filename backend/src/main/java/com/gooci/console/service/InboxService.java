package com.gooci.console.service;

import com.gooci.console.web.dto.InboxBoardView;
import com.gooci.console.web.dto.InboxIdsCommand;
import com.gooci.console.web.dto.InboxTickerView;

import java.util.List;

/**
 * @ClassName InboxService
 * @Description 站内信。不做小程序自动推送
 * @author GooCi
 * @date 2026-03-26
 */
public interface InboxService {

    InboxBoardView load(String readFlag, String rangeCode, String appCode, long pageNo, long pageSize);

    InboxBoardView markRead(InboxIdsCommand command);

    InboxBoardView remove(InboxIdsCommand command);

    long unreadCount();

    List<InboxTickerView> ticker();
}
