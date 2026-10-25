package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.EnterRecord;
import com.gooci.console.domain.SysMenu;
import com.gooci.console.domain.SysParameter;
import com.gooci.console.domain.SysUser;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.EnterRecordMapper;
import com.gooci.console.mapper.SysMenuMapper;
import com.gooci.console.mapper.SysParameterMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.security.TokenSupport;
import com.gooci.console.service.AuthService;
import com.gooci.console.service.InboxService;
import com.gooci.console.service.TodoService;
import com.gooci.console.support.MenuTreeSupport;
import com.gooci.console.support.SmsCodeStore;
import com.gooci.console.web.dto.ConsoleOverview;
import com.gooci.console.web.dto.LoginCommand;
import com.gooci.console.web.dto.RegisterCommand;
import com.gooci.console.web.dto.SessionView;
import com.gooci.console.web.dto.SmsCodeCommand;
import com.gooci.console.web.dto.SmsCodeView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @ClassName AuthServiceImpl
 * @Description 登录与工作台会话
 * @author GooCi
 * @date 2026-03-23
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String KIND_ENTERPRISE = "ENTERPRISE";
    private static final String KIND_PERSON = "PERSON";
    private static final String ADMIN_ROLE = "企业管理员";
    private static final String STAFF_ROLE = "员工";

    private final SysUserMapper sysUserMapper;
    private final EnterRecordMapper enterRecordMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysParameterMapper sysParameterMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenSupport tokenSupport;
    private final SmsCodeStore smsCodeStore;
    private final InboxService inboxService;
    private final TodoService todoService;

    public AuthServiceImpl(
            SysUserMapper sysUserMapper,
            EnterRecordMapper enterRecordMapper,
            DeptRecordMapper deptRecordMapper,
            SysMenuMapper sysMenuMapper,
            SysParameterMapper sysParameterMapper,
            PasswordEncoder passwordEncoder,
            TokenSupport tokenSupport,
            SmsCodeStore smsCodeStore,
            InboxService inboxService,
            TodoService todoService) {
        this.sysUserMapper = sysUserMapper;
        this.enterRecordMapper = enterRecordMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysParameterMapper = sysParameterMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenSupport = tokenSupport;
        this.smsCodeStore = smsCodeStore;
        this.inboxService = inboxService;
        this.todoService = todoService;
    }

    @Override
    public SessionView login(LoginCommand command) {
        if (command == null || command.getLoginName() == null) {
            throw new BusinessException("账号不能为空");
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, command.getLoginName()));
        if (user == null || !passwordEncoder.matches(command.getLoginPassword(), user.getLoginPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        SessionView view = buildSession(user);
        view.setToken(tokenSupport.issue(user.getId()));
        return view;
    }

    @Override
    public SmsCodeView sendSms(SmsCodeCommand command) {
        String mobile = command.getMobile().trim();
        if (!mobile.matches("1\\d{10}")) {
            throw new BusinessException("请填写正确的手机号");
        }
        SmsCodeView view = new SmsCodeView();
        view.setDemoCode(smsCodeStore.issue(mobile));
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionView register(RegisterCommand command) {
        if (command.getAgreed() == null || !command.getAgreed()) {
            throw new BusinessException("请阅读并同意平台协议");
        }
        String mobile = command.getMobile().trim();
        if (!mobile.matches("1\\d{10}")) {
            throw new BusinessException("请填写正确的手机号");
        }
        if (!smsCodeStore.match(mobile, command.getSmsCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        String password = command.getLoginPassword().trim();
        if (password.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        if (!password.equals(command.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long exist = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, mobile));
        if (exist != null && exist > 0) {
            throw new BusinessException("该手机号已注册");
        }
        String kind = command.getRegisterKind().trim().toUpperCase(Locale.ROOT);
        SysUser user;
        if (KIND_ENTERPRISE.equals(kind)) {
            user = registerEnterprise(command, mobile, password);
        } else if (KIND_PERSON.equals(kind)) {
            user = registerPerson(command, mobile, password);
        } else {
            throw new BusinessException("注册类型不正确");
        }
        smsCodeStore.consume(mobile);
        SessionView view = buildSession(user);
        view.setToken(tokenSupport.issue(user.getId()));
        return view;
    }

    @Override
    public SessionView currentSession() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SessionView view = buildSession(user);
        view.setToken(null);
        return view;
    }

    @Override
    public ConsoleOverview overview() {
        ConsoleOverview overview = new ConsoleOverview();
        overview.setUnreadMessageCount((int) inboxService.unreadCount());
        overview.setPendingTaskCount((int) todoService.pendingCount());
        overview.setLinkedDeviceCount(0);
        overview.setOnlineDeviceCount(0);
        overview.setRecentMessages(inboxService.ticker());
        return overview;
    }

    private SessionView buildSession(SysUser user) {
        SessionView view = new SessionView();
        view.setUserId(user.getId());
        view.setDisplayName(user.getDisplayName());
        view.setMobile(maskMobile(user.getMobile()));
        view.setRoleName(user.getRoleName());
        view.setEnterId(user.getTsEnterId());
        EnterRecord enter = user.getTsEnterId() == null ? null : enterRecordMapper.selectById(user.getTsEnterId());
        if (enter != null) {
            view.setEnterName(enter.getEnterName());
            view.setRegionText(enter.getRegionText());
            view.setCertStatus(enter.getCertStatus());
        }
        view.setServicePhone(readServicePhone());
        List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortNo));
        view.setMenus(MenuTreeSupport.toTree(menus));
        return view;
    }

    private String readServicePhone() {
        SysParameter parameter = sysParameterMapper.selectOne(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        if (parameter == null) {
            return "";
        }
        return parameter.getParamValue() == null ? "" : parameter.getParamValue();
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    private SysUser registerEnterprise(RegisterCommand command, String mobile, String password) {
        String enterName = trimToNull(command.getEnterName());
        if (enterName == null) {
            throw new BusinessException("公司名称不能为空");
        }
        EnterRecord enter = createEnter(enterName, command.getDisplayName().trim(), mobile);
        return createUser(mobile, password, command.getDisplayName().trim(), null, ADMIN_ROLE, enter.getId());
    }

    private SysUser registerPerson(RegisterCommand command, String mobile, String password) {
        String jobName = trimToNull(command.getJobName());
        if (jobName == null) {
            throw new BusinessException("请选择职能");
        }
        String invite = trimToNull(command.getInviteCode());
        if (invite != null) {
            EnterRecord enter = enterRecordMapper.selectOne(new LambdaQueryWrapper<EnterRecord>()
                    .eq(EnterRecord::getEnterCode, invite));
            if (enter == null) {
                throw new BusinessException("邀请码无效");
            }
            return createUser(mobile, password, command.getDisplayName().trim(), jobName, STAFF_ROLE, enter.getId());
        }
        EnterRecord enter = createEnter(command.getDisplayName().trim(), command.getDisplayName().trim(), mobile);
        return createUser(mobile, password, command.getDisplayName().trim(), jobName, ADMIN_ROLE, enter.getId());
    }

    private EnterRecord createEnter(String enterName, String contactName, String mobile) {
        EnterRecord enter = new EnterRecord();
        enter.setId(UUID.randomUUID().toString());
        enter.setEnterCode("E" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        enter.setEnterName(enterName);
        enter.setCertStatus("0");
        enter.setContactName(contactName);
        enter.setContactMobile(mobile);
        enter.setRegisterTime(LocalDateTime.now());
        enter.setBizCertStatus("0");
        enter.setBankCertStatus("0");
        enter.setCertEnterName(enterName);
        enter.setTsEnterId(enter.getId());
        enterRecordMapper.insert(enter);
        DeptRecord dept = new DeptRecord();
        dept.setId(UUID.randomUUID().toString());
        dept.setDeptCode("DEFAULT");
        dept.setDeptName("默认部门");
        dept.setEnableFlag("1");
        dept.setSortNo(10);
        dept.setTsEnterId(enter.getId());
        deptRecordMapper.insert(dept);
        return enter;
    }

    private SysUser createUser(String mobile, String password, String displayName, String jobName, String roleName, String enterId) {
        SysUser user = new SysUser();
        user.setId(UUID.randomUUID().toString());
        user.setLoginName(mobile);
        user.setLoginPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setMobile(mobile);
        user.setJobName(jobName);
        user.setRoleName(roleName);
        user.setTsEnterId(enterId);
        sysUserMapper.insert(user);
        return user;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
