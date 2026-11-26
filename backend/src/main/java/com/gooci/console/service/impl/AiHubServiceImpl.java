package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.AiFamily;
import com.gooci.console.common.BusinessException;
import com.gooci.console.common.ExtTargetSupport;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.SysUser;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.AiHubService;
import com.gooci.console.web.dto.AiHubCatalogItem;
import com.gooci.console.web.dto.AiHubView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @ClassName AiHubServiceImpl
 * @Description 中台绑定与 AI 功能目录。目录统一开放，不按砖块隐藏裁剪
 * @author GooCi
 * @date 2026-03-26
 */
@Service
public class AiHubServiceImpl implements AiHubService {

    public static final String APP_CODE = AiFamily.HUB;

    private final SysUserMapper sysUserMapper;
    private final AppOpenMapper appOpenMapper;
    private final boolean aiHubLocalForward;
    private final ExtTargetSupport.Policy targetPolicy;

    public AiHubServiceImpl(
            SysUserMapper sysUserMapper,
            AppOpenMapper appOpenMapper,
            @Value("${gooci.ext-allow-hosts:}") String allowHosts,
            @Value("${gooci.ext-public-https-only:true}") boolean publicHttpsOnly,
            @Value("${gooci.ai-hub-local-forward:false}") boolean aiHubLocalForward) {
        this.sysUserMapper = sysUserMapper;
        this.appOpenMapper = appOpenMapper;
        this.aiHubLocalForward = aiHubLocalForward;
        this.targetPolicy = ExtTargetSupport.policy(allowHosts, publicHttpsOnly);
    }

    @Override
    public AiHubView load() {
        String enterId = requireEnterId();
        AppOpen hub = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .eq(AppOpen::getAppCode, APP_CODE));
        AiHubView view = new AiHubView();
        view.setAppCode(APP_CODE);
        view.setRuntime("deepseek-harness");
        view.setProfile("web");
        view.setFramePath("/ext/" + APP_CODE + "/");
        view.setLaunchUrl("");
        String target = hub == null ? "" : hub.getTargetUrl();
        String deny = ExtTargetSupport.denyReason(target, targetPolicy, aiHubLocalForward);
        view.setBound(deny == null);
        view.setDeny(deny == null ? "" : deny);
        for (AiFamily.Fn fn : AiFamily.FUNCTIONS) {
            AiHubCatalogItem item = new AiHubCatalogItem();
            item.setAppCode(fn.code());
            item.setAppName(fn.name());
            item.setEntryPath(AiFamily.entry(fn.code()));
            view.getCatalog().add(item);
        }
        return view;
    }

    private String requireEnterId() {
        if (LoginUserHolder.ops()) {
            throw new BusinessException("请使用企业账号查看中台");
        }
        String userId = LoginUserHolder.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getTsEnterId() == null || user.getTsEnterId().isBlank()) {
            throw new BusinessException("未登录");
        }
        return user.getTsEnterId();
    }
}
