package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.PermService;
import com.gooci.console.web.dto.AppAuthBoardView;
import com.gooci.console.web.dto.AppAuthDetailView;
import com.gooci.console.web.dto.AppRoleBoardView;
import com.gooci.console.web.dto.SaveAppAuthCommand;
import com.gooci.console.web.dto.SaveAppRoleCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName PermController
 * @Description APP 功能角色、数据角色、账号授权。查询即筛选
 * @author GooCi
 * @date 2026-03-25
 */
@RestController
@RequestMapping("/api/console/perm")
public class PermController {

    private final PermService permService;

    public PermController(PermService permService) {
        this.permService = permService;
    }

    @GetMapping("/roles")
    public ApiResult<AppRoleBoardView> roles(
            @RequestParam String kind,
            @RequestParam(required = false) String appCode,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "5") long pageSize) {
        return ApiResult.ok(permService.loadRoles(kind, appCode, pageNo, pageSize));
    }

    @PostMapping("/roles")
    public ApiResult<AppRoleBoardView> createRole(@Valid @RequestBody SaveAppRoleCommand command) {
        return ApiResult.ok(permService.createRole(command));
    }

    @GetMapping("/auth")
    public ApiResult<AppAuthBoardView> auth(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "5") long pageSize) {
        return ApiResult.ok(permService.loadAuth(keyword, pageNo, pageSize));
    }

    @GetMapping("/auth/detail")
    public ApiResult<AppAuthDetailView> detail(@RequestParam String userId) {
        return ApiResult.ok(permService.loadAuthDetail(userId));
    }

    @PostMapping("/auth")
    public ApiResult<AppAuthBoardView> saveAuth(@Valid @RequestBody SaveAppAuthCommand command) {
        return ApiResult.ok(permService.saveAuth(command));
    }
}
