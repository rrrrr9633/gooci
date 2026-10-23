package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.InboxService;
import com.gooci.console.web.dto.InboxBoardView;
import com.gooci.console.web.dto.InboxIdsCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName InboxController
 * @Description 站内信。与工作台消息动态同源，无自动推送
 * @author GooCi
 * @date 2026-03-26
 */
@RestController
@RequestMapping("/api/console/inbox")
public class InboxController {

    private final InboxService inboxService;

    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping
    public ApiResult<InboxBoardView> list(
            @RequestParam(defaultValue = "0") String readFlag,
            @RequestParam(defaultValue = "ALL") String rangeCode,
            @RequestParam(required = false) String appCode,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResult.ok(inboxService.load(readFlag, rangeCode, appCode, pageNo, pageSize));
    }

    @PostMapping("/read")
    public ApiResult<InboxBoardView> markRead(@Valid @RequestBody InboxIdsCommand command) {
        return ApiResult.ok(inboxService.markRead(command));
    }

    @PostMapping("/remove")
    public ApiResult<InboxBoardView> remove(@Valid @RequestBody InboxIdsCommand command) {
        return ApiResult.ok(inboxService.remove(command));
    }
}
