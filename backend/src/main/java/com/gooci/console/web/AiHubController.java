package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.AiHubService;
import com.gooci.console.web.dto.AiHubView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName AiHubController
 * @Description 企业侧查看 AI 中台绑定和统一开放的功能目录。不返回下一跳原始地址
 * @author GooCi
 * @date 2026-03-26
 */
@RestController
@RequestMapping("/api/console/ai")
public class AiHubController {

    private final AiHubService aiHubService;

    public AiHubController(AiHubService aiHubService) {
        this.aiHubService = aiHubService;
    }

    @GetMapping("/hub")
    public ApiResult<AiHubView> hub() {
        return ApiResult.ok(aiHubService.load());
    }
}
