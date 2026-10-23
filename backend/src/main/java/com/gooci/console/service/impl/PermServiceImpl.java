package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.AppRole;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserAppGrant;
import com.gooci.console.domain.UserDeptRel;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.AppRoleMapper;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserAppGrantMapper;
import com.gooci.console.mapper.UserDeptRelMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.PermService;
import com.gooci.console.web.dto.AppAuthBoardView;
import com.gooci.console.web.dto.AppAuthDetailView;
import com.gooci.console.web.dto.AppAuthItemView;
import com.gooci.console.web.dto.AppAuthPickCommand;
import com.gooci.console.web.dto.AppCatalogView;
import com.gooci.console.web.dto.AppGrantView;
import com.gooci.console.web.dto.AppOpenView;
import com.gooci.console.web.dto.AppRoleBoardView;
import com.gooci.console.web.dto.AppRoleRowView;
import com.gooci.console.web.dto.SaveAppAuthCommand;
import com.gooci.console.web.dto.SaveAppRoleCommand;
import com.gooci.console.web.dto.SliceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName PermServiceImpl
 * @Description 已开通 APP 的角色目录与账号授权。查询只做筛选
 * @author GooCi
 * @date 2026-03-25
 */
@Service
public class PermServiceImpl implements PermService {

    static final String KIND_FUNC = "FUNC";
    static final String KIND_DATA = "DATA";

    private final SysUserMapper sysUserMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final UserDeptRelMapper userDeptRelMapper;
    private final AppOpenMapper appOpenMapper;
    private final AppRoleMapper appRoleMapper;
    private final UserAppGrantMapper userAppGrantMapper;

    public PermServiceImpl(
            SysUserMapper sysUserMapper,
            DeptRecordMapper deptRecordMapper,
            UserDeptRelMapper userDeptRelMapper,
            AppOpenMapper appOpenMapper,
            AppRoleMapper appRoleMapper,
            UserAppGrantMapper userAppGrantMapper) {
        this.sysUserMapper = sysUserMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.userDeptRelMapper = userDeptRelMapper;
        this.appOpenMapper = appOpenMapper;
        this.appRoleMapper = appRoleMapper;
        this.userAppGrantMapper = userAppGrantMapper;
    }

    @Override
    public AppRoleBoardView loadRoles(String kind, String appCode, long pageNo, long pageSize) {
        String enterId = requireEnterId();
        String roleKind = requireKind(kind);
        List<AppOpen> opens = listOpens(enterId);
        Map<String, String> appNames = appNames(opens);
        String filter = trimToNull(appCode);
        List<AppRole> roles = appRoleMapper.selectList(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getRoleKind, roleKind)
                .eq(filter != null, AppRole::getAppCode, filter)
                .orderByAsc(AppRole::getAppCode)
                .orderByAsc(AppRole::getSortNo)
                .orderByAsc(AppRole::getCrtTime));
        List<AppRoleRowView> rows = new ArrayList<>();
        for (AppRole role : roles) {
            if (!appNames.containsKey(role.getAppCode())) {
                continue;
            }
            AppRoleRowView row = new AppRoleRowView();
            row.setId(role.getId());
            row.setAppCode(role.getAppCode());
            row.setAppName(appNames.get(role.getAppCode()));
            row.setRoleName(role.getRoleName());
            rows.add(row);
        }
        AppRoleBoardView board = new AppRoleBoardView();
        board.setApps(toOpenViews(opens));
        board.setSlice(slice(rows, pageNo, pageSize <= 0 ? 5 : pageSize));
        return board;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppRoleBoardView createRole(SaveAppRoleCommand command) {
        String enterId = requireEnterId();
        String roleKind = requireKind(command.getKind());
        String appCode = command.getAppCode().trim();
        String roleName = command.getRoleName().trim();
        if (roleName.length() > 100) {
            throw new BusinessException("角色名称过长");
        }
        AppOpen open = requireOpen(enterId, appCode);
        Long exist = appRoleMapper.selectCount(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getAppCode, appCode)
                .eq(AppRole::getRoleKind, roleKind)
                .eq(AppRole::getRoleName, roleName));
        if (exist != null && exist > 0) {
            throw new BusinessException(KIND_FUNC.equals(roleKind) ? "该APP已存在同名功能角色" : "该APP已存在同名数据角色");
        }
        AppRole role = new AppRole();
        role.setId(UUID.randomUUID().toString());
        role.setAppCode(open.getAppCode());
        role.setRoleKind(roleKind);
        role.setRoleName(roleName);
        role.setSortNo(nextSort(enterId, appCode, roleKind));
        role.setTsEnterId(enterId);
        appRoleMapper.insert(role);
        return loadRoles(roleKind, null, 1, 5);
    }

    @Override
    public AppAuthBoardView loadAuth(String keyword, long pageNo, long pageSize) {
        String enterId = requireEnterId();
        Map<String, String> deptNames = deptNames(enterId);
        Map<String, String> userDept = userDeptNames(enterId, deptNames);
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId)
                .orderByAsc(SysUser::getCrtTime));
        String key = keyword == null ? "" : keyword.trim();
        List<AppAuthItemView> rows = new ArrayList<>();
        for (SysUser user : users) {
            if (!matchUser(user, key)) {
                continue;
            }
            AppAuthItemView row = new AppAuthItemView();
            row.setUserId(user.getId());
            row.setMobile(nz(user.getMobile()));
            row.setDisplayName(nz(user.getDisplayName()));
            row.setDeptName(nz(userDept.get(user.getId())));
            row.setMailAddr(nz(user.getMailAddr()));
            rows.add(row);
        }
        AppAuthBoardView board = new AppAuthBoardView();
        board.setApps(toCatalogs(enterId));
        board.setSlice(slice(rows, pageNo, pageSize <= 0 ? 5 : pageSize));
        return board;
    }

    @Override
    public AppAuthDetailView loadAuthDetail(String userId) {
        String enterId = requireEnterId();
        SysUser user = requireEnterUser(enterId, userId);
        Map<String, String> deptNames = deptNames(enterId);
        Map<String, String> userDept = userDeptNames(enterId, deptNames);
        AppAuthDetailView view = new AppAuthDetailView();
        view.setUserId(user.getId());
        view.setDisplayName(nz(user.getDisplayName()));
        view.setMobile(nz(user.getMobile()));
        view.setMailAddr(nz(user.getMailAddr()));
        view.setRoleName(nz(user.getRoleName()));
        view.setDeptName(nz(userDept.get(user.getId())));
        view.setGrants(loadGrants(enterId, user.getId()));
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppAuthBoardView saveAuth(SaveAppAuthCommand command) {
        String enterId = requireEnterId();
        String kind = requireKind(command.getKind());
        SysUser target = requireEnterUser(enterId, command.getUserId());
        Map<String, AppOpen> opens = new HashMap<>();
        for (AppOpen open : listOpens(enterId)) {
            opens.put(open.getAppCode(), open);
        }
        Map<String, Set<String>> allowed = roleNames(enterId, kind);
        Map<String, AppAuthPickCommand> picked = new HashMap<>();
        for (AppAuthPickCommand item : command.getItems()) {
            if (item == null || item.getAppCode() == null || item.getAppCode().isBlank()) {
                continue;
            }
            String appCode = item.getAppCode().trim();
            if (!opens.containsKey(appCode)) {
                throw new BusinessException("存在未开通的APP，无法授权");
            }
            picked.put(appCode, item);
        }
        List<UserAppGrant> grants = userAppGrantMapper.selectList(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTmSysUserId, target.getId())
                .eq(UserAppGrant::getTsEnterId, enterId));
        Map<String, UserAppGrant> byApp = new HashMap<>();
        for (UserAppGrant grant : grants) {
            byApp.put(grant.getAppCode(), grant);
        }
        for (Map.Entry<String, AppAuthPickCommand> entry : picked.entrySet()) {
            String appCode = entry.getKey();
            String joined = joinRoles(entry.getValue().getRoleNames(), allowed.get(appCode));
            UserAppGrant grant = byApp.get(appCode);
            if (grant == null) {
                grant = new UserAppGrant();
                grant.setId(UUID.randomUUID().toString());
                grant.setTmSysUserId(target.getId());
                grant.setAppCode(appCode);
                grant.setAppName(opens.get(appCode).getAppName());
                grant.setTsEnterId(enterId);
                applyKind(grant, kind, joined);
                userAppGrantMapper.insert(grant);
                byApp.put(appCode, grant);
            } else {
                grant.setAppName(opens.get(appCode).getAppName());
                applyKind(grant, kind, joined);
                userAppGrantMapper.updateById(grant);
            }
        }
        for (UserAppGrant grant : new ArrayList<>(byApp.values())) {
            if (picked.containsKey(grant.getAppCode())) {
                continue;
            }
            applyKind(grant, kind, "");
            if (blank(grant.getFuncRoleName()) && blank(grant.getDataRoleName())) {
                userAppGrantMapper.deleteById(grant.getId());
            } else {
                userAppGrantMapper.updateById(grant);
            }
        }
        return loadAuth(null, 1, 5);
    }

    private void applyKind(UserAppGrant grant, String kind, String joined) {
        if (KIND_FUNC.equals(kind)) {
            grant.setFuncRoleName(joined);
        } else {
            grant.setDataRoleName(joined);
        }
    }

    private String joinRoles(List<String> names, Set<String> allowed) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        LinkedHashSet<String> kept = new LinkedHashSet<>();
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String text = name.trim();
            if (allowed != null && !allowed.contains(text)) {
                throw new BusinessException("存在未定义的角色，无法授权");
            }
            kept.add(text);
        }
        return String.join("、", kept);
    }

    private Map<String, Set<String>> roleNames(String enterId, String kind) {
        List<AppRole> roles = appRoleMapper.selectList(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getRoleKind, kind));
        Map<String, Set<String>> map = new HashMap<>();
        for (AppRole role : roles) {
            map.computeIfAbsent(role.getAppCode(), key -> new HashSet<>()).add(role.getRoleName());
        }
        return map;
    }

    private List<AppGrantView> loadGrants(String enterId, String userId) {
        Map<String, String> opened = appNames(listOpens(enterId));
        List<UserAppGrant> rows = userAppGrantMapper.selectList(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTmSysUserId, userId)
                .orderByAsc(UserAppGrant::getAppName));
        List<AppGrantView> grants = new ArrayList<>();
        for (UserAppGrant row : rows) {
            if (!opened.containsKey(row.getAppCode())) {
                continue;
            }
            AppGrantView item = new AppGrantView();
            item.setAppCode(row.getAppCode());
            item.setAppName(opened.get(row.getAppCode()));
            item.setFuncRoleName(nz(row.getFuncRoleName()));
            item.setDataRoleName(nz(row.getDataRoleName()));
            grants.add(item);
        }
        return grants;
    }

    private List<AppCatalogView> toCatalogs(String enterId) {
        List<AppOpen> opens = listOpens(enterId);
        List<AppRole> roles = appRoleMapper.selectList(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .orderByAsc(AppRole::getSortNo)
                .orderByAsc(AppRole::getCrtTime));
        Map<String, AppCatalogView> byCode = new HashMap<>();
        List<AppCatalogView> list = new ArrayList<>();
        for (AppOpen open : opens) {
            AppCatalogView view = new AppCatalogView();
            view.setAppCode(open.getAppCode());
            view.setAppName(open.getAppName());
            byCode.put(open.getAppCode(), view);
            list.add(view);
        }
        for (AppRole role : roles) {
            AppCatalogView view = byCode.get(role.getAppCode());
            if (view == null) {
                continue;
            }
            if (KIND_FUNC.equals(role.getRoleKind())) {
                view.getFuncRoles().add(role.getRoleName());
            } else if (KIND_DATA.equals(role.getRoleKind())) {
                view.getDataRoles().add(role.getRoleName());
            }
        }
        return list;
    }

    private List<AppOpen> listOpens(String enterId) {
        List<AppOpen> rows = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .orderByAsc(AppOpen::getSortNo)
                .orderByAsc(AppOpen::getCrtTime));
        List<AppOpen> opened = new ArrayList<>();
        for (AppOpen open : rows) {
            if (open.getEntryPath() == null || open.getEntryPath().isBlank()) {
                continue;
            }
            if ("1".equals(open.getHideFlag())) {
                continue;
            }
            opened.add(open);
        }
        return opened;
    }

    private List<AppOpenView> toOpenViews(List<AppOpen> opens) {
        List<AppOpenView> views = new ArrayList<>();
        for (AppOpen open : opens) {
            AppOpenView view = new AppOpenView();
            view.setAppCode(open.getAppCode());
            view.setAppName(open.getAppName());
            views.add(view);
        }
        return views;
    }

    private Map<String, String> appNames(List<AppOpen> opens) {
        Map<String, String> map = new HashMap<>();
        for (AppOpen open : opens) {
            map.put(open.getAppCode(), open.getAppName());
        }
        return map;
    }

    private AppOpen requireOpen(String enterId, String appCode) {
        if (appCode == null || appCode.isBlank()) {
            throw new BusinessException("请选择已开通APP");
        }
        String code = appCode.trim();
        for (AppOpen open : listOpens(enterId)) {
            if (code.equals(open.getAppCode())) {
                return open;
            }
        }
        throw new BusinessException("该APP未开通");
    }

    private int nextSort(String enterId, String appCode, String kind) {
        List<AppRole> roles = appRoleMapper.selectList(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getAppCode, appCode)
                .eq(AppRole::getRoleKind, kind)
                .orderByDesc(AppRole::getSortNo));
        if (roles.isEmpty() || roles.get(0).getSortNo() == null) {
            return 10;
        }
        return roles.get(0).getSortNo() + 10;
    }

    private Map<String, String> deptNames(String enterId) {
        List<DeptRecord> depts = deptRecordMapper.selectList(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId));
        Map<String, String> map = new HashMap<>();
        for (DeptRecord dept : depts) {
            map.put(dept.getId(), dept.getDeptName());
        }
        return map;
    }

    private Map<String, String> userDeptNames(String enterId, Map<String, String> deptNames) {
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTsEnterId, enterId));
        Map<String, List<String>> grouped = new HashMap<>();
        for (UserDeptRel rel : rels) {
            String name = deptNames.get(rel.getTmDeptId());
            if (name == null || name.isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(rel.getTmSysUserId(), key -> new ArrayList<>()).add(name);
        }
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            map.put(entry.getKey(), String.join("、", entry.getValue()));
        }
        return map;
    }

    private boolean matchUser(SysUser user, String key) {
        if (key.isEmpty()) {
            return true;
        }
        String mobile = nz(user.getMobile());
        String name = nz(user.getDisplayName());
        return mobile.contains(key) || name.contains(key);
    }

    private String requireKind(String kind) {
        String text = kind == null ? "" : kind.trim().toUpperCase();
        if (KIND_FUNC.equals(text) || KIND_DATA.equals(text)) {
            return text;
        }
        throw new BusinessException("角色类型不正确");
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

    private SysUser requireUser() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private String requireEnterId() {
        SysUser user = requireUser();
        if (user.getTsEnterId() == null) {
            throw new BusinessException("未绑定企业");
        }
        return user.getTsEnterId();
    }

    private <T> SliceView<T> slice(List<T> all, long pageNo, long pageSize) {
        long size = pageSize <= 0 ? 5 : pageSize;
        long page = pageNo <= 0 ? 1 : pageNo;
        int from = (int) ((page - 1) * size);
        SliceView<T> view = new SliceView<>();
        view.setTotal(all.size());
        view.setPageNo(page);
        view.setPageSize(size);
        if (from >= all.size()) {
            return view;
        }
        int to = Math.min(all.size(), from + (int) size);
        view.setRecords(new ArrayList<>(all.subList(from, to)));
        return view;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
