package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.AuthService;
import com.gooci.console.web.dto.ConsoleOverview;
import com.gooci.console.web.dto.SessionView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName ConsoleController
 * @Description 工作台会话与占位概览
 * @author GooCi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/console")
public class ConsoleController {

    private final AuthService authService;

    public ConsoleController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/session")
    public ApiResult<SessionView> session() {
        return ApiResult.ok(authService.currentSession());
    }

    @GetMapping("/overview")
    public ApiResult<ConsoleOverview> overview() {
        return ApiResult.ok(authService.overview());
    }
}
