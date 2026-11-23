package com.gooci.console.service;

import com.gooci.console.web.dto.AiHubView;

/**
 * @ClassName AiHubService
 * @Description 工业控制台看 AI 中台绑定。运行时在其他云上的 deepseek-harness，不在本机起进程
 * @author GooCi
 * @date 2026-03-26
 */
public interface AiHubService {

    AiHubView load();
}
