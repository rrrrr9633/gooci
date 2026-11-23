package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName TodoItemView
 * @Description 待办列表行
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class TodoItemView {

    private String id;

    private String bizLine;

    private String taskType;

    private String content;

    private String senderName;

    private String handlePath;

    private String statusFlag;
}
