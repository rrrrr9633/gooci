package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.OpsService;
import com.gooci.console.web.dto.ImplantOpsAppCommand;
import com.gooci.console.web.dto.LoginCommand;
import com.gooci.console.web.dto.OpsAppRowView;
import com.gooci.console.web.dto.OpsChangeRowView;
import com.gooci.console.web.dto.OpsEnterRowView;
import com.gooci.console.web.dto.OpsIconRowView;
import com.gooci.console.web.dto.OpsSessionView;
import com.gooci.console.web.dto.OpsSiteView;
import com.gooci.console.web.dto.SaveOpsAppCommand;
import com.gooci.console.web.dto.SaveOpsSiteCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName OpsController
 * @Description 厂商运营台接口。不进企业工作台
 * @author GooCi
 * @date 2026-03-26
 */
@RestController
@RequestMapping("/api/ops")
public class OpsController {

    private final OpsService opsService;

    public OpsController(OpsService opsService) {
        this.opsService = opsService;
    }

    @PostMapping("/auth/login")
    public ApiResult<OpsSessionView> login(@Valid @RequestBody LoginCommand command) {
        return ApiResult.ok(opsService.login(command));
    }

    @GetMapping("/session")
    public ApiResult<OpsSessionView> session() {
        return ApiResult.ok(opsService.session());
    }

    @GetMapping("/site")
    public ApiResult<OpsSiteView> site() {
        return ApiResult.ok(opsService.loadSite());
    }

    @PostMapping("/site")
    public ApiResult<OpsSiteView> saveSite(@Valid @RequestBody SaveOpsSiteCommand command) {
        return ApiResult.ok(opsService.saveSite(command));
    }

    @GetMapping("/enters")
    public ApiResult<List<OpsEnterRowView>> enters() {
        return ApiResult.ok(opsService.listEnters());
    }

    @GetMapping("/apps")
    public ApiResult<List<OpsAppRowView>> apps(@RequestParam String enterId) {
        return ApiResult.ok(opsService.listApps(enterId));
    }

    @PostMapping("/apps")
    public ApiResult<List<OpsAppRowView>> saveApp(@Valid @RequestBody SaveOpsAppCommand command) {
        return ApiResult.ok(opsService.saveApp(command));
    }

    @PostMapping("/apps/implant")
    public ApiResult<List<OpsAppRowView>> implant(@Valid @RequestBody ImplantOpsAppCommand command) {
        return ApiResult.ok(opsService.implant(command));
    }

    @GetMapping("/icons")
    public ApiResult<List<OpsIconRowView>> icons() {
        return ApiResult.ok(opsService.listIcons());
    }

    @GetMapping("/changes")
    public ApiResult<List<OpsChangeRowView>> changes() {
        return ApiResult.ok(opsService.listChanges());
    }
}
