package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName InboxBoardView
 * @Description 站内信看板
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class InboxBoardView {

    private List<AppOpenView> apps = new ArrayList<>();

    private SliceView<InboxItemView> slice = new SliceView<>();
}
