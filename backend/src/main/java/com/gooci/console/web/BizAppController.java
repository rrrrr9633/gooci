package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.BizAppService;
import com.gooci.console.web.dto.BizAppBoardView;
import com.gooci.console.web.dto.SaveBizAppAdminCommand;
import com.gooci.console.web.dto.SaveBizAppHideCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BizAppController
 * @Description 工业 APP 快捷入口。授权码不做
 * @author GooCi
 * @date 2026-03-26
 */
@RestController
@RequestMapping("/api/console/biz/apps")
public class BizAppController {

    private final BizAppService bizAppService;

    public BizAppController(BizAppService bizAppService) {
        this.bizAppService = bizAppService;
    }

    @GetMapping
    public ApiResult<BizAppBoardView> board() {
        return ApiResult.ok(bizAppService.loadBoard());
    }

    @PostMapping("/hide")
    public ApiResult<BizAppBoardView> hide(@Valid @RequestBody SaveBizAppHideCommand command) {
        return ApiResult.ok(bizAppService.saveHide(command));
    }

    @PostMapping("/admin")
    public ApiResult<BizAppBoardView> admin(@Valid @RequestBody SaveBizAppAdminCommand command) {
        return ApiResult.ok(bizAppService.saveAdmin(command));
    }
}
