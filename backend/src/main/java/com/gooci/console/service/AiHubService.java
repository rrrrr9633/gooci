package com.gooci.console.service;

import com.gooci.console.web.dto.AiHubView;

/**
 * @ClassName AiHubService
 * @Description 工业控制台看 AI 中台绑定。界面是 deepseek-harness 的 web，门口只转发
 * @author GooCi
 * @date 2026-03-26
 */
public interface AiHubService {

    AiHubView load();
}
