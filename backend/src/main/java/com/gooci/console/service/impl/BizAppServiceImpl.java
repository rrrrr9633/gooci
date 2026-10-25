package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.AppRole;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserAppGrant;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.AppRoleMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserAppGrantMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.BizAppService;
import com.gooci.console.web.dto.BizAppBoardView;
import com.gooci.console.web.dto.BizAppHideItem;
import com.gooci.console.web.dto.BizAppTileView;
import com.gooci.console.web.dto.SaveBizAppAdminCommand;
import com.gooci.console.web.dto.SaveBizAppHideCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName BizAppServiceImpl
 * @Description 工业 APP 入口来自植入清单。无入口路径的不算快捷入口
 * @author GooCi
 * @date 2026-03-26
 */
@Service
public class BizAppServiceImpl implements BizAppService {

    private final SysUserMapper sysUserMapper;
    private final AppOpenMapper appOpenMapper;
    private final AppRoleMapper appRoleMapper;
    private final UserAppGrantMapper userAppGrantMapper;

    public BizAppServiceImpl(
            SysUserMapper sysUserMapper,
            AppOpenMapper appOpenMapper,
            AppRoleMapper appRoleMapper,
            UserAppGrantMapper userAppGrantMapper) {
        this.sysUserMapper = sysUserMapper;
        this.appOpenMapper = appOpenMapper;
        this.appRoleMapper = appRoleMapper;
        this.userAppGrantMapper = userAppGrantMapper;
    }

    @Override
    public BizAppBoardView loadBoard() {
        return toBoard(listPortal(requireEnterId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizAppBoardView saveHide(SaveBizAppHideCommand command) {
        String enterId = requireEnterId();
        List<AppOpen> portal = listPortal(enterId);
        List<BizAppHideItem> items = command.getItems() == null ? List.of() : command.getItems();
        for (AppOpen open : portal) {
            String hide = "0";
            for (BizAppHideItem item : items) {
                if (item == null || item.getAppCode() == null) {
                    continue;
                }
                if (open.getAppCode().equals(item.getAppCode().trim())) {
                    hide = "1".equals(item.getHideFlag()) ? "1" : "0";
                    break;
                }
            }
            open.setHideFlag(hide);
            appOpenMapper.updateById(open);
        }
        return toBoard(listPortal(enterId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizAppBoardView saveAdmin(SaveBizAppAdminCommand command) {
        String enterId = requireEnterId();
        AppOpen open = requirePortal(enterId, command.getAppCode());
        SysUser target = requireEnterUser(enterId, command.getUserId());
        String roleName = command.getRoleName().trim();
        AppRole role = appRoleMapper.selectOne(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getAppCode, open.getAppCode())
                .eq(AppRole::getRoleKind, "FUNC")
                .eq(AppRole::getRoleName, roleName));
        if (role == null) {
            throw new BusinessException("该模块没有这个功能角色");
        }
        UserAppGrant grant = userAppGrantMapper.selectOne(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTmSysUserId, target.getId())
                .eq(UserAppGrant::getTsEnterId, enterId)
                .eq(UserAppGrant::getAppCode, open.getAppCode()));
        if (grant == null) {
            grant = new UserAppGrant();
            grant.setId(UUID.randomUUID().toString());
            grant.setTmSysUserId(target.getId());
            grant.setAppCode(open.getAppCode());
            grant.setAppName(open.getAppName());
            grant.setFuncRoleName(roleName);
            grant.setTsEnterId(enterId);
            userAppGrantMapper.insert(grant);
        } else {
            grant.setAppName(open.getAppName());
            grant.setFuncRoleName(mergeRole(grant.getFuncRoleName(), roleName));
            userAppGrantMapper.updateById(grant);
        }
        return toBoard(listPortal(enterId));
    }

    private List<AppOpen> listPortal(String enterId) {
        List<AppOpen> opens = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .orderByAsc(AppOpen::getSortNo)
                .orderByAsc(AppOpen::getCrtTime));
        List<AppOpen> portal = new ArrayList<>();
        for (AppOpen open : opens) {
            if (open.getEntryPath() == null || open.getEntryPath().isBlank()) {
                continue;
            }
            if ("1".equals(open.getHideFlag())) {
                continue;
            }
            portal.add(open);
        }
        return portal;
    }

    private AppOpen requirePortal(String enterId, String appCode) {
        if (appCode == null || appCode.isBlank()) {
            throw new BusinessException("请选择模块");
        }
        String code = appCode.trim();
        for (AppOpen open : listPortal(enterId)) {
            if (code.equals(open.getAppCode())) {
                return open;
            }
        }
        throw new BusinessException("该模块尚未植入入口");
    }

    private BizAppBoardView toBoard(List<AppOpen> portal) {
        BizAppBoardView board = new BizAppBoardView();
        for (AppOpen open : portal) {
            BizAppTileView tile = new BizAppTileView();
            tile.setAppCode(open.getAppCode());
            tile.setAppName(open.getAppName());
            tile.setEntryPath(open.getEntryPath());
            tile.setHideFlag("1".equals(open.getHideFlag()) ? "1" : "0");
            board.getApps().add(tile);
        }
        return board;
    }

    private String mergeRole(String current, String roleName) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (current != null && !current.isBlank()) {
            for (String part : current.split("、")) {
                if (!part.isBlank()) {
                    names.add(part.trim());
                }
            }
        }
        names.add(roleName);
        return String.join("、", names);
    }

    private SysUser requireEnterUser(String enterId, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("请选择人员");
        }
        SysUser user = sysUserMapper.selectById(userId.trim());
        if (user == null || !enterId.equals(user.getTsEnterId())) {
            throw new BusinessException("人员不存在");
        }
        return user;
    }

    private String requireEnterId() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getTsEnterId() == null) {
            throw new BusinessException("未绑定企业");
        }
        return user.getTsEnterId();
    }
}
