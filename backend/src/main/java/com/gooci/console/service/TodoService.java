package com.gooci.console.service;

import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.TodoItemView;

/**
 * @ClassName TodoService
 * @Description 待办任务。原理未定，只提供当前用户列表
 * @author GooCi
 * @date 2026-03-26
 */
public interface TodoService {

    SliceView<TodoItemView> load(long pageNo, long pageSize);

    long pendingCount();
}
