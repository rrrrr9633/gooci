package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.EnterRecord;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserAppGrant;
import com.gooci.console.domain.UserDeptRel;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.EnterRecordMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserAppGrantMapper;
import com.gooci.console.mapper.UserDeptRelMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.ProfileService;
import com.gooci.console.web.dto.AppGrantView;
import com.gooci.console.web.dto.ProfileView;
import com.gooci.console.web.dto.UpdateMailCommand;
import com.gooci.console.web.dto.UpdateNameCommand;
import com.gooci.console.web.dto.UpdatePasswordCommand;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName ProfileServiceImpl
 * @Description 当前用户个人信息读写
 * @author GooCi
 * @date 2026-03-23
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Pattern MAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SysUserMapper sysUserMapper;
    private final EnterRecordMapper enterRecordMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final UserDeptRelMapper userDeptRelMapper;
    private final UserAppGrantMapper userAppGrantMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(
            SysUserMapper sysUserMapper,
            EnterRecordMapper enterRecordMapper,
            DeptRecordMapper deptRecordMapper,
            UserDeptRelMapper userDeptRelMapper,
            UserAppGrantMapper userAppGrantMapper,
            PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.enterRecordMapper = enterRecordMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.userDeptRelMapper = userDeptRelMapper;
        this.userAppGrantMapper = userAppGrantMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProfileView loadProfile() {
        return toView(requireUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileView updateName(UpdateNameCommand command) {
        SysUser user = requireUser();
        String name = command.getDisplayName().trim();
        if (name.length() > 100) {
            throw new BusinessException("姓名过长");
        }
        user.setDisplayName(name);
        sysUserMapper.updateById(user);
        return toView(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileView updateMail(UpdateMailCommand command) {
        SysUser user = requireUser();
        String mail = command.getMailAddr() == null ? "" : command.getMailAddr().trim();
        if (!mail.isEmpty() && !MAIL_PATTERN.matcher(mail).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        String stored = mail.isEmpty() ? null : mail;
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getMailAddr, stored));
        user.setMailAddr(stored);
        return toView(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordCommand command) {
        SysUser user = requireUser();
        if (!passwordEncoder.matches(command.getOldPassword(), user.getLoginPassword())) {
            throw new BusinessException("原密码错误");
        }
        String next = command.getNewPassword().trim();
        if (next.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        if (passwordEncoder.matches(next, user.getLoginPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        user.setLoginPassword(passwordEncoder.encode(next));
        sysUserMapper.updateById(user);
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

    private ProfileView toView(SysUser user) {
        ProfileView view = new ProfileView();
        view.setDisplayName(user.getDisplayName());
        view.setMobile(user.getMobile());
        view.setMailAddr(user.getMailAddr() == null ? "" : user.getMailAddr());
        view.setRoleName(user.getRoleName());
        if (user.getTsEnterId() != null) {
            EnterRecord enter = enterRecordMapper.selectById(user.getTsEnterId());
            if (enter != null) {
                view.setEnterName(enter.getEnterName());
            }
        }
        view.setDeptNames(loadDeptNames(user.getId()));
        view.setGrants(loadGrants(user.getId()));
        return view;
    }

    private List<String> loadDeptNames(String userId) {
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, userId));
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> deptIds = rels.stream().map(UserDeptRel::getTmDeptId).collect(Collectors.toList());
        List<DeptRecord> depts = deptRecordMapper.selectList(new LambdaQueryWrapper<DeptRecord>()
                .in(DeptRecord::getId, deptIds)
                .orderByAsc(DeptRecord::getDeptCode));
        Map<String, DeptRecord> byId = depts.stream()
                .collect(Collectors.toMap(DeptRecord::getId, Function.identity(), (a, b) -> a));
        List<String> names = new ArrayList<>();
        for (UserDeptRel rel : rels) {
            DeptRecord dept = byId.get(rel.getTmDeptId());
            if (dept != null) {
                names.add(dept.getDeptName());
            }
        }
        return names;
    }

    private List<AppGrantView> loadGrants(String userId) {
        List<UserAppGrant> rows = userAppGrantMapper.selectList(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTmSysUserId, userId)
                .orderByAsc(UserAppGrant::getAppName));
        List<AppGrantView> grants = new ArrayList<>();
        for (UserAppGrant row : rows) {
            AppGrantView item = new AppGrantView();
            item.setAppCode(row.getAppCode());
            item.setAppName(row.getAppName());
            item.setFuncRoleName(row.getFuncRoleName());
            item.setDataRoleName(row.getDataRoleName());
            grants.add(item);
        }
        return grants;
    }
}
