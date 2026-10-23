package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.TodoService;
import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.TodoItemView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TodoController
 * @Description 待办任务空壳列表
 * @author GooCi
 * @date 2026-03-26
 */
@RestController
@RequestMapping("/api/console/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ApiResult<SliceView<TodoItemView>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResult.ok(todoService.load(pageNo, pageSize));
    }
}
