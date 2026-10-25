package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.common.ExtTargetSupport;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.EnterRecord;
import com.gooci.console.domain.OpsUser;
import com.gooci.console.domain.SysParameter;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.EnterRecordMapper;
import com.gooci.console.mapper.OpsUserMapper;
import com.gooci.console.mapper.SysParameterMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.security.TokenSupport;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName OpsServiceImpl
 * @Description 厂商运营台读写。不进企业菜单
 * @author GooCi
 * @date 2026-03-26
 */
@Service
public class OpsServiceImpl implements OpsService {

    private final OpsUserMapper opsUserMapper;
    private final EnterRecordMapper enterRecordMapper;
    private final AppOpenMapper appOpenMapper;
    private final SysParameterMapper sysParameterMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenSupport tokenSupport;

    public OpsServiceImpl(
            OpsUserMapper opsUserMapper,
            EnterRecordMapper enterRecordMapper,
            AppOpenMapper appOpenMapper,
            SysParameterMapper sysParameterMapper,
            PasswordEncoder passwordEncoder,
            TokenSupport tokenSupport) {
        this.opsUserMapper = opsUserMapper;
        this.enterRecordMapper = enterRecordMapper;
        this.appOpenMapper = appOpenMapper;
        this.sysParameterMapper = sysParameterMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenSupport = tokenSupport;
    }

    @Override
    public OpsSessionView login(LoginCommand command) {
        if (command.getLoginName() == null || command.getLoginName().isBlank()) {
            throw new BusinessException("账号不能为空");
        }
        OpsUser user = opsUserMapper.selectOne(new LambdaQueryWrapper<OpsUser>()
                .eq(OpsUser::getLoginName, command.getLoginName().trim()));
        if (user == null || !passwordEncoder.matches(command.getLoginPassword(), user.getLoginPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        return toSession(user, tokenSupport.issue(user.getId(), "ops"));
    }

    @Override
    public OpsSessionView session() {
        return toSession(requireOps(), null);
    }

    @Override
    public OpsSiteView loadSite() {
        requireOps();
        return siteView();
    }

    @Override
    @Transactional
    public OpsSiteView saveSite(SaveOpsSiteCommand command) {
        OpsUser ops = requireOps();
        String phone = command.getServicePhone() == null ? "" : command.getServicePhone().trim();
        SysParameter parameter = sysParameterMapper.selectOne(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        if (parameter == null) {
            throw new BusinessException("客服电话参数不存在");
        }
        parameter.setParamValue(phone);
        parameter.setUpdUsrId(ops.getId());
        parameter.setUpdUsrName(ops.getDisplayName());
        parameter.setUpdTime(LocalDateTime.now());
        sysParameterMapper.updateById(parameter);
        return siteView();
    }

    @Override
    public List<OpsEnterRowView> listEnters() {
        requireOps();
        List<EnterRecord> enters = enterRecordMapper.selectList(new LambdaQueryWrapper<EnterRecord>()
                .orderByDesc(EnterRecord::getCrtTime));
        List<AppOpen> opens = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>());
        Map<String, int[]> counts = new LinkedHashMap<>();
        for (AppOpen open : opens) {
            if (!implanted(open)) {
                continue;
            }
            int[] pair = counts.computeIfAbsent(open.getTsEnterId(), key -> new int[2]);
            pair[0]++;
            if (!"1".equals(open.getHideFlag())) {
                pair[1]++;
            }
        }
        List<OpsEnterRowView> rows = new ArrayList<>();
        for (EnterRecord enter : enters) {
            OpsEnterRowView row = new OpsEnterRowView();
            row.setId(enter.getId());
            row.setEnterCode(enter.getEnterCode());
            row.setEnterName(enter.getEnterName());
            row.setCertStatus(enter.getCertStatus());
            row.setRegionText(enter.getRegionText());
            int[] pair = counts.getOrDefault(enter.getId(), new int[2]);
            row.setAppCount(pair[0]);
            row.setVisibleAppCount(pair[1]);
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<OpsAppRowView> listApps(String enterId) {
        requireOps();
        return appsOf(enterId);
    }

    @Override
    @Transactional
    public List<OpsAppRowView> saveApp(SaveOpsAppCommand command) {
        requireOps();
        EnterRecord enter = enterRecordMapper.selectById(command.getEnterId());
        if (enter == null) {
            throw new BusinessException("企业不存在");
        }
        AppOpen open = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enter.getId())
                .eq(AppOpen::getAppCode, command.getAppCode().trim()));
        if (open == null) {
            throw new BusinessException("该企业未植入此模块");
        }
        String hide = "1".equals(command.getHideFlag()) ? "1" : "0";
        String target = command.getTargetUrl() == null ? "" : command.getTargetUrl().trim();
        if (!target.isBlank()) {
            checkTarget(target);
        }
        open.setHideFlag(hide);
        open.setTargetUrl(target);
        open.setUpdTime(LocalDateTime.now());
        appOpenMapper.updateById(open);
        return appsOf(enter.getId());
    }

    @Override
    @Transactional
    public List<OpsAppRowView> implant(ImplantOpsAppCommand command) {
        OpsUser ops = requireOps();
        EnterRecord enter = enterRecordMapper.selectById(command.getEnterId());
        if (enter == null) {
            throw new BusinessException("企业不存在");
        }
        String code = command.getAppCode().trim();
        if (!code.matches("[A-Za-z0-9_]{2,40}")) {
            throw new BusinessException("模块代码只允许字母数字下划线");
        }
        String name = command.getAppName().trim();
        String entry = command.getEntryPath() == null || command.getEntryPath().isBlank()
                ? "/workbench/ext/" + code
                : command.getEntryPath().trim();
        if (!entry.startsWith("/")) {
            throw new BusinessException("入口路径必须以 / 开头");
        }
        String target = command.getTargetUrl() == null ? "" : command.getTargetUrl().trim();
        if (!target.isBlank()) {
            checkTarget(target);
        }
        AppOpen open = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enter.getId())
                .eq(AppOpen::getAppCode, code));
        LocalDateTime now = LocalDateTime.now();
        if (open == null) {
            open = new AppOpen();
            open.setId(UUID.randomUUID().toString());
            open.setAppCode(code);
            open.setAppName(name);
            open.setSortNo(nextSort(enter.getId()));
            open.setHideFlag("0");
            open.setEntryPath(entry);
            open.setTargetUrl(target);
            open.setTsEnterId(enter.getId());
            open.setCrtUsrId(ops.getId());
            open.setCrtUsrName(ops.getDisplayName());
            open.setCrtTime(now);
            open.setUpdUsrId(ops.getId());
            open.setUpdUsrName(ops.getDisplayName());
            open.setUpdTime(now);
            appOpenMapper.insert(open);
        } else {
            open.setAppName(name);
            open.setEntryPath(entry);
            open.setTargetUrl(target);
            open.setHideFlag("0");
            open.setUpdUsrId(ops.getId());
            open.setUpdUsrName(ops.getDisplayName());
            open.setUpdTime(now);
            appOpenMapper.updateById(open);
        }
        return appsOf(enter.getId());
    }

    @Override
    public List<OpsIconRowView> listIcons() {
        requireOps();
        List<AppOpen> opens = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .orderByAsc(AppOpen::getSortNo)
                .orderByAsc(AppOpen::getAppName));
        Map<String, OpsIconRowView> unique = new LinkedHashMap<>();
        for (AppOpen open : opens) {
            if (!implanted(open) || open.getAppCode() == null || open.getAppCode().isBlank()) {
                continue;
            }
            unique.computeIfAbsent(open.getAppCode(), code -> {
                OpsIconRowView row = new OpsIconRowView();
                row.setAppCode(code);
                row.setAppName(open.getAppName());
                return row;
            });
        }
        return new ArrayList<>(unique.values());
    }

    @Override
    public List<OpsChangeRowView> listChanges() {
        requireOps();
        List<EnterRecord> enters = enterRecordMapper.selectList(new LambdaQueryWrapper<EnterRecord>());
        List<OpsChangeRowView> rows = new ArrayList<>();
        for (EnterRecord enter : enters) {
            if (hasPending(enter.getBizCertStatus(), enter.getBizRemark())) {
                rows.add(changeRow(enter, "工商登记变更", enter.getBizRemark()));
            }
            if (hasPending(enter.getBankCertStatus(), enter.getBankRemark())) {
                rows.add(changeRow(enter, "银行账户变更", enter.getBankRemark()));
            }
        }
        return rows;
    }

    private List<OpsAppRowView> appsOf(String enterId) {
        if (enterId == null || enterId.isBlank()) {
            throw new BusinessException("请选择企业");
        }
        List<AppOpen> opens = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .orderByAsc(AppOpen::getSortNo)
                .orderByAsc(AppOpen::getAppName));
        List<OpsAppRowView> rows = new ArrayList<>();
        for (AppOpen open : opens) {
            if (!implanted(open)) {
                continue;
            }
            ExtTargetSupport.Probe probe = ExtTargetSupport.probe(open.getTargetUrl());
            OpsAppRowView row = new OpsAppRowView();
            row.setAppCode(open.getAppCode());
            row.setAppName(open.getAppName());
            row.setHideFlag(open.getHideFlag() == null ? "0" : open.getHideFlag());
            row.setEntryPath(open.getEntryPath());
            row.setTargetUrl(open.getTargetUrl());
            row.setHealth(probe.health());
            row.setHealthHint(probe.hint());
            rows.add(row);
        }
        return rows;
    }

    private boolean implanted(AppOpen open) {
        return open.getEntryPath() != null && !open.getEntryPath().isBlank();
    }

    private int nextSort(String enterId) {
        List<AppOpen> opens = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId));
        int max = 0;
        for (AppOpen open : opens) {
            if (open.getSortNo() != null && open.getSortNo() > max) {
                max = open.getSortNo();
            }
        }
        return max + 10;
    }

    private void checkTarget(String target) {
        URI uri;
        try {
            uri = URI.create(target);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("目标地址不正确");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BusinessException("目标地址只允许 http/https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new BusinessException("目标地址缺少主机");
        }
    }

    private boolean hasPending(String status, String remark) {
        if ("0".equals(status)) {
            return true;
        }
        return remark != null && !remark.isBlank();
    }

    private OpsChangeRowView changeRow(EnterRecord enter, String kind, String remark) {
        OpsChangeRowView row = new OpsChangeRowView();
        row.setEnterId(enter.getId());
        row.setEnterName(enter.getEnterName());
        row.setKind(kind);
        row.setRemark(remark == null ? "" : remark);
        return row;
    }

    private OpsSiteView siteView() {
        SysParameter parameter = sysParameterMapper.selectOne(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        OpsSiteView view = new OpsSiteView();
        view.setBrandName("GOOCI 谷器");
        view.setServicePhone(parameter == null || parameter.getParamValue() == null ? "" : parameter.getParamValue());
        return view;
    }

    private OpsSessionView toSession(OpsUser user, String token) {
        OpsSessionView view = new OpsSessionView();
        view.setToken(token);
        view.setUserId(user.getId());
        view.setDisplayName(user.getDisplayName());
        view.setScope("ops");
        return view;
    }

    private OpsUser requireOps() {
        if (!LoginUserHolder.ops()) {
            throw new BusinessException("未登录");
        }
        String userId = LoginUserHolder.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("未登录");
        }
        OpsUser user = opsUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
