package com.gooci.console.seed;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.AppRole;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.DeviceCodeRule;
import com.gooci.console.domain.DeviceRecord;
import com.gooci.console.domain.EnterRecord;
import com.gooci.console.domain.OpsUser;
import com.gooci.console.domain.StaffCodeRule;
import com.gooci.console.domain.SysMenu;
import com.gooci.console.domain.SysParameter;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserAppGrant;
import com.gooci.console.domain.UserDeptRel;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.AppRoleMapper;
import com.gooci.console.mapper.DeviceCodeRuleMapper;
import com.gooci.console.mapper.DeviceRecordMapper;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.EnterRecordMapper;
import com.gooci.console.mapper.OpsUserMapper;
import com.gooci.console.mapper.StaffCodeRuleMapper;
import com.gooci.console.mapper.SysMenuMapper;
import com.gooci.console.mapper.SysParameterMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserAppGrantMapper;
import com.gooci.console.mapper.UserDeptRelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName KernelDataSeed
 * @Description 首次启动写入演示企业、管理员与内核菜单
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class KernelDataSeed implements ApplicationRunner {

    private final EnterRecordMapper enterRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysParameterMapper sysParameterMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final UserDeptRelMapper userDeptRelMapper;
    private final UserAppGrantMapper userAppGrantMapper;
    private final AppOpenMapper appOpenMapper;
    private final AppRoleMapper appRoleMapper;
    private final DeviceRecordMapper deviceRecordMapper;
    private final DeviceCodeRuleMapper deviceCodeRuleMapper;
    private final StaffCodeRuleMapper staffCodeRuleMapper;
    private final OpsUserMapper opsUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final String extPdmUrl;
    private final String extPackUrl;

    public KernelDataSeed(
            EnterRecordMapper enterRecordMapper,
            SysUserMapper sysUserMapper,
            SysMenuMapper sysMenuMapper,
            SysParameterMapper sysParameterMapper,
            DeptRecordMapper deptRecordMapper,
            UserDeptRelMapper userDeptRelMapper,
            UserAppGrantMapper userAppGrantMapper,
            AppOpenMapper appOpenMapper,
            AppRoleMapper appRoleMapper,
            DeviceRecordMapper deviceRecordMapper,
            DeviceCodeRuleMapper deviceCodeRuleMapper,
            StaffCodeRuleMapper staffCodeRuleMapper,
            OpsUserMapper opsUserMapper,
            PasswordEncoder passwordEncoder,
            @Value("${gooci.ext-pdm-url:http://127.0.0.1:5174}") String extPdmUrl,
            @Value("${gooci.ext-pack-url:http://127.0.0.1:8777}") String extPackUrl) {
        this.enterRecordMapper = enterRecordMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysParameterMapper = sysParameterMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.userDeptRelMapper = userDeptRelMapper;
        this.userAppGrantMapper = userAppGrantMapper;
        this.appOpenMapper = appOpenMapper;
        this.appRoleMapper = appRoleMapper;
        this.deviceRecordMapper = deviceRecordMapper;
        this.deviceCodeRuleMapper = deviceCodeRuleMapper;
        this.staffCodeRuleMapper = staffCodeRuleMapper;
        this.opsUserMapper = opsUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.extPdmUrl = extPdmUrl;
        this.extPackUrl = extPackUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        String enterId = ensureEnter();
        String userId = ensureUser(enterId);
        ensureMenus();
        ensureParameter();
        ensureDepts(enterId, userId);
        ensureAppCatalog(enterId, userId);
        ensureAppGrants(enterId, userId);
        ensureOrgDemo(enterId, userId);
        ensureStaffDemo(enterId, userId);
        ensureDemoAuth(enterId);
        ensureOpsUser();
    }

    private String ensureEnter() {
        EnterRecord exist = enterRecordMapper.selectOne(new LambdaQueryWrapper<EnterRecord>()
                .eq(EnterRecord::getEnterCode, "DEMO"));
        if (exist != null) {
            fillEnterBlank(exist);
            return exist.getId();
        }
        EnterRecord enter = new EnterRecord();
        enter.setId(UUID.randomUUID().toString());
        enter.setEnterCode("DEMO");
        enter.setEnterName("谷器演示工厂");
        enter.setCertStatus("1");
        enter.setRegionText("待完善地区信息");
        enter.setContactName("管理员");
        enter.setContactMobile("13800000000");
        enter.setRegisterTime(LocalDateTime.of(2024, 12, 20, 17, 54, 28));
        enter.setBizCertStatus("1");
        enter.setBankCertStatus("1");
        enter.setCertEnterName("谷器演示工厂");
        fillAudit(enter, enter.getId());
        enterRecordMapper.insert(enter);
        return enter.getId();
    }

    private void fillEnterBlank(EnterRecord exist) {
        boolean dirty = false;
        if (exist.getContactName() == null) {
            exist.setContactName("管理员");
            dirty = true;
        }
        if (exist.getContactMobile() == null) {
            exist.setContactMobile("13800000000");
            dirty = true;
        }
        if (exist.getRegisterTime() == null) {
            exist.setRegisterTime(LocalDateTime.of(2024, 12, 20, 17, 54, 28));
            dirty = true;
        }
        if (exist.getBizCertStatus() == null) {
            exist.setBizCertStatus("1");
            dirty = true;
        }
        if (exist.getBankCertStatus() == null) {
            exist.setBankCertStatus("1");
            dirty = true;
        }
        if (exist.getCertEnterName() == null || exist.getCertEnterName().isBlank()) {
            exist.setCertEnterName(exist.getEnterName());
            dirty = true;
        }
        if (dirty) {
            enterRecordMapper.updateById(exist);
        }
    }

    private String ensureUser(String enterId) {
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, "admin"));
        if (exist != null) {
            return exist.getId();
        }
        SysUser user = new SysUser();
        user.setId(UUID.randomUUID().toString());
        user.setLoginName("admin");
        user.setLoginPassword(passwordEncoder.encode("GooCi@2026"));
        user.setDisplayName("管理员");
        user.setMobile("13800000000");
        user.setRoleName("企业管理员");
        user.setTsEnterId(enterId);
        fillAudit(user, user.getId());
        sysUserMapper.insert(user);
        return user.getId();
    }

    private void ensureMenus() {
        String workbench = ensureMenu(null, "workbench", "工作台", "/workbench/home", "monitor", 10);
        ensureMenu(workbench, "workbench-home", "工作台", "/workbench/home", null, 11);
        ensureMenu(workbench, "workbench-profile", "个人信息", "/workbench/profile", null, 12);

        String org = ensureMenu(null, "org", "组织管理", "/workbench/org/enterprise", "office", 20);
        ensureMenu(org, "org-enterprise", "企业信息", "/workbench/org/enterprise", null, 21);
        ensureMenu(org, "org-structure", "企业组织管理", "/workbench/org/structure", null, 22);
        ensureMenu(org, "org-cert", "实名认证", "/workbench/org/cert", null, 23);
        ensureMenu(org, "org-dept", "企业部门管理", "/workbench/org/dept", null, 24);

        String resource = ensureMenu(null, "resource", "资源管理", "/workbench/resource/staff", "collection", 30);
        ensureMenu(resource, "resource-staff", "员工管理", "/workbench/resource/staff", null, 31);
        ensureMenu(resource, "resource-device", "设备管理", "/workbench/resource/device", null, 32);

        String perm = ensureMenu(null, "perm", "权限管理", "/workbench/perm/app-func", "lock", 40);
        ensureMenu(perm, "perm-func", "APP功能角色", "/workbench/perm/app-func", null, 41);
        ensureMenu(perm, "perm-data", "APP数据角色", "/workbench/perm/app-data", null, 42);
        ensureMenu(perm, "perm-auth", "APP账号授权", "/workbench/perm/app-auth", null, 43);

        String biz = ensureMenu(null, "biz", "业务中心", "/workbench/biz/app", "grid", 50);
        ensureMenu(biz, "biz-app", "工业APP", "/workbench/biz/app", null, 51);
        ensureMenu(biz, "biz-todo", "待办任务", "/workbench/biz/todo", null, 52);
        ensureMenu(biz, "biz-inbox", "站内信", "/workbench/biz/inbox", null, 53);
    }

    private void ensureDepts(String enterId, String userId) {
        List<DeptRecord> exist = deptRecordMapper.selectList(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId));
        Map<String, DeptRecord> byCode = new HashMap<>();
        for (DeptRecord item : exist) {
            byCode.put(item.getDeptCode(), item);
        }
        String[] codes = {"WORKSHOP", "SALES", "PURCHASE", "WAREHOUSE"};
        String[] names = {"生产车间", "销售", "采购", "仓库"};
        int[] sorts = {10, 20, 30, 40};
        List<DeptRecord> boundDepts = new ArrayList<>(exist);
        for (int i = 0; i < codes.length; i++) {
            DeptRecord found = byCode.get(codes[i]);
            if (found != null) {
                if (found.getSortNo() == null || found.getSortNo() == 0) {
                    found.setSortNo(sorts[i]);
                    found.setEnableFlag("1");
                    deptRecordMapper.updateById(found);
                }
                continue;
            }
            DeptRecord dept = new DeptRecord();
            dept.setId(UUID.randomUUID().toString());
            dept.setDeptCode(codes[i]);
            dept.setDeptName(names[i]);
            dept.setEnableFlag("1");
            dept.setSortNo(sorts[i]);
            dept.setTsEnterId(enterId);
            fillAudit(dept, userId);
            deptRecordMapper.insert(dept);
            boundDepts.add(dept);
        }
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, userId));
        Set<String> boundIds = new HashSet<>();
        for (UserDeptRel rel : rels) {
            boundIds.add(rel.getTmDeptId());
        }
        for (DeptRecord dept : boundDepts) {
            if (boundIds.contains(dept.getId())) {
                continue;
            }
            UserDeptRel rel = new UserDeptRel();
            rel.setId(UUID.randomUUID().toString());
            rel.setTmSysUserId(userId);
            rel.setTmDeptId(dept.getId());
            rel.setTsEnterId(enterId);
            fillAudit(rel, userId);
            userDeptRelMapper.insert(rel);
        }
    }

    private void ensureAppCatalog(String enterId, String userId) {
        retireOpenApp(enterId, "DEVICE_EYE_T01");
        retireOpenApp(enterId, "DEVICE_EYE");
        retireOpenApp(enterId, "APP_STEWARD");
        retireOpenApp(enterId, "DEMO_SHELL");
        ensureOpenApp(enterId, userId, "EXT_PDM", "设备故障预测", 30, "/workbench/ext/EXT_PDM", extPdmUrl);
        ensureOpenApp(enterId, userId, "EXT_PACK_AI", "礼盒包装AI", 40, "/workbench/ext/EXT_PACK_AI", extPackUrl);
        ensureAppRole(enterId, userId, "EXT_PDM", "FUNC", "超级管理员", 10);
        ensureAppRole(enterId, userId, "EXT_PACK_AI", "FUNC", "超级管理员", 10);
        EnterRecord enter = enterRecordMapper.selectById(enterId);
        String dataRole = enter == null || enter.getEnterName() == null ? "谷器演示工厂" : enter.getEnterName();
        ensureAppRole(enterId, userId, "EXT_PDM", "DATA", dataRole, 10);
        ensureAppRole(enterId, userId, "EXT_PACK_AI", "DATA", dataRole, 10);
    }

    private void ensureOpenApp(String enterId, String userId, String code, String name, int sortNo, String entryPath, String targetUrl) {
        AppOpen exist = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .eq(AppOpen::getAppCode, code));
        if (exist != null) {
            boolean dirty = false;
            if (entryPath != null && (exist.getEntryPath() == null || exist.getEntryPath().isBlank())) {
                exist.setEntryPath(entryPath);
                exist.setHideFlag(exist.getHideFlag() == null ? "0" : exist.getHideFlag());
                dirty = true;
            }
            if (targetUrl != null && !targetUrl.equals(exist.getTargetUrl())) {
                exist.setTargetUrl(targetUrl);
                dirty = true;
            }
            if (dirty) {
                appOpenMapper.updateById(exist);
            }
            return;
        }
        AppOpen open = new AppOpen();
        open.setId(UUID.randomUUID().toString());
        open.setAppCode(code);
        open.setAppName(name);
        open.setSortNo(sortNo);
        open.setHideFlag("0");
        open.setEntryPath(entryPath);
        open.setTargetUrl(targetUrl);
        open.setTsEnterId(enterId);
        fillAudit(open, userId);
        appOpenMapper.insert(open);
    }

    private void hideOpenApp(String enterId, String code) {
        AppOpen exist = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .eq(AppOpen::getAppCode, code));
        if (exist == null) {
            return;
        }
        appOpenMapper.update(null, new LambdaUpdateWrapper<AppOpen>()
                .eq(AppOpen::getId, exist.getId())
                .set(AppOpen::getHideFlag, "1")
                .set(AppOpen::getEntryPath, null)
                .set(AppOpen::getTargetUrl, null));
    }

    private void retireOpenApp(String enterId, String code) {
        AppOpen exist = appOpenMapper.selectOne(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .eq(AppOpen::getAppCode, code));
        if (exist != null) {
            hideOpenApp(enterId, code);
            appOpenMapper.deleteById(exist.getId());
        }
        appRoleMapper.delete(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getAppCode, code));
        userAppGrantMapper.delete(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTsEnterId, enterId)
                .eq(UserAppGrant::getAppCode, code));
    }

    private void ensureAppRole(String enterId, String userId, String appCode, String kind, String roleName, int sortNo) {
        AppRole exist = appRoleMapper.selectOne(new LambdaQueryWrapper<AppRole>()
                .eq(AppRole::getTsEnterId, enterId)
                .eq(AppRole::getAppCode, appCode)
                .eq(AppRole::getRoleKind, kind)
                .eq(AppRole::getRoleName, roleName));
        if (exist != null) {
            return;
        }
        AppRole role = new AppRole();
        role.setId(UUID.randomUUID().toString());
        role.setAppCode(appCode);
        role.setRoleKind(kind);
        role.setRoleName(roleName);
        role.setSortNo(sortNo);
        role.setTsEnterId(enterId);
        fillAudit(role, userId);
        appRoleMapper.insert(role);
    }

    private void ensureDemoAuth(String enterId) {
        SysUser river = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, "13800002001"));
        if (river == null) {
            return;
        }
        EnterRecord enter = enterRecordMapper.selectById(enterId);
        String dataRole = enter == null ? "" : enter.getEnterName();
        ensureGrant(river.getId(), enterId, "EXT_PDM", "设备故障预测", "超级管理员", dataRole);
    }

    private void ensureAppGrants(String enterId, String userId) {
        EnterRecord enter = enterRecordMapper.selectById(enterId);
        String dataRole = enter == null ? "" : enter.getEnterName();
        ensureGrant(userId, enterId, "EXT_PDM", "设备故障预测", "超级管理员", dataRole);
        ensureGrant(userId, enterId, "EXT_PACK_AI", "礼盒包装AI", "超级管理员", dataRole);
    }

    private void ensureGrant(String userId, String enterId, String code, String name, String funcRole, String dataRole) {
        Long count = userAppGrantMapper.selectCount(new LambdaQueryWrapper<UserAppGrant>()
                .eq(UserAppGrant::getTmSysUserId, userId)
                .eq(UserAppGrant::getAppCode, code));
        if (count != null && count > 0) {
            return;
        }
        insertGrant(userId, enterId, code, name, funcRole, dataRole);
    }

    private void insertGrant(String userId, String enterId, String code, String name, String funcRole, String dataRole) {
        UserAppGrant row = new UserAppGrant();
        row.setId(UUID.randomUUID().toString());
        row.setTmSysUserId(userId);
        row.setAppCode(code);
        row.setAppName(name);
        row.setFuncRoleName(funcRole);
        row.setDataRoleName(dataRole);
        row.setTsEnterId(enterId);
        fillAudit(row, userId);
        userAppGrantMapper.insert(row);
    }

    private void ensureOrgDemo(String enterId, String adminUserId) {
        String[][] admins = {
                {"13900001001", "刘芳", ""},
                {"13900001002", "王强", ""},
                {"13900001003", "陈敏", "chenmin@gooci.demo"},
                {"13900001004", "赵磊", ""}
        };
        for (String[] row : admins) {
            ensureNamedUser(enterId, row[0], row[1], row[2], "企业管理员");
        }
        DeptRecord workshop = deptRecordMapper.selectOne(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId)
                .eq(DeptRecord::getDeptCode, "WORKSHOP"));
        if (workshop == null) {
            return;
        }
        String[][] members = {
                {"13800002001", "李清河", ""},
                {"13800002002", "张明", ""},
                {"13800002003", "刘建军", ""},
                {"13800002004", "王也", ""},
                {"13800002005", "黄明", ""},
                {"13800002006", "周宁", ""},
                {"13800002007", "吴倩", ""},
                {"13800002008", "郑浩", ""},
                {"13800002009", "冯雪", ""},
                {"13800002010", "曹阳", ""}
        };
        for (String[] row : members) {
            SysUser user = ensureNamedUser(enterId, row[0], row[1], row[2], "员工");
            bindDept(enterId, adminUserId, user.getId(), workshop.getId());
        }
        SysUser owner = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, "13800002001"));
        String ownerId = owner == null ? null : owner.getId();
        String[][] devices = {
                {"GOOCI-SN-1001", "数控3#", "BOX-D01", "GSK", "988T", "7", "数控车床", "1", "A"},
                {"GOOCI-SN-1002", "数控1#", "BOX-D02", "GSK", "980T", "8", "数控车床", "1", "A"},
                {"GOOCI-SN-1003", "数控2#", "BOX-D03", "GSK", "980T", "7", "数控车床", "1", "B"},
                {"GOOCI-SN-1004", "数控7#", "BOX-D04", "GSK", "980T", "7", "数控车床", "1", "A"},
                {"GOOCI-SN-1005", "数控6#", "BOX-D05", "GSK", "980T", "7", "数控车床", "1", "B"},
                {"GOOCI-SN-1006", "数控4#", "BOX-D06", "GSK", "980T", "6", "数控车床", "1", "B"},
                {"GOOCI-SN-1007", "数控5#", "BOX-D07", "GSK", "980T", "6", "数控车床", "1", "C"},
                {"GOOCI-SN-1008", "数控8#", "BOX-D08", "GSK", "988T", "5", "数控车床", "1", "C"},
                {"GOOCI-SN-1009", "数控9#", "BOX-D09", "GSK", "988T", "5", "数控车床", "1", "C"},
                {"GOOCI-SN-1010", "立加4#", "BOX-D10", "GSK", "VMC", "0", "加工中心", "0", "B"},
                {"GOOCI-SN-1011", "立加1#", "BOX-D11", "GSK", "VMC", "4", "加工中心", "1", "A"},
                {"GOOCI-SN-1012", "立加2#", "BOX-D12", "GSK", "VMC", "4", "加工中心", "1", "A"},
                {"GOOCI-SN-1013", "立加3#", "BOX-D13", "GSK", "VMC", "3", "加工中心", "1", "B"}
        };
        int seq = 0;
        for (String[] row : devices) {
            seq++;
            String code = "EQP20260324" + String.format("%04d", seq);
            DeviceRecord exist = deviceRecordMapper.selectOne(new LambdaQueryWrapper<DeviceRecord>()
                    .eq(DeviceRecord::getDeviceSn, row[0]));
            boolean bound = !"0".equals(row[7]);
            if (exist == null) {
                DeviceRecord device = new DeviceRecord();
                device.setId(UUID.randomUUID().toString());
                fillDeviceRow(device, enterId, adminUserId, workshop.getId(), ownerId, row, code, bound);
                deviceRecordMapper.insert(device);
            } else if (exist.getDeviceCode() == null || exist.getDeviceCode().isBlank()) {
                fillDeviceRow(exist, enterId, adminUserId, bound ? workshop.getId() : exist.getTmDeptId(),
                        ownerId, row, code, bound);
                deviceRecordMapper.updateById(exist);
            }
        }
        DeviceCodeRule rule = deviceCodeRuleMapper.selectOne(new LambdaQueryWrapper<DeviceCodeRule>()
                .eq(DeviceCodeRule::getTsEnterId, enterId));
        if (rule == null) {
            rule = new DeviceCodeRule();
            rule.setId(UUID.randomUUID().toString());
            rule.setCodeName("设备编号");
            rule.setSeqNo(seq);
            rule.setTsEnterId(enterId);
            fillAudit(rule, adminUserId);
            deviceCodeRuleMapper.insert(rule);
        } else if (rule.getSeqNo() == null || rule.getSeqNo() < seq) {
            rule.setSeqNo(seq);
            deviceCodeRuleMapper.updateById(rule);
        }
    }

    private void fillDeviceRow(
            DeviceRecord device,
            String enterId,
            String adminUserId,
            String workshopId,
            String ownerId,
            String[] row,
            String code,
            boolean bound) {
        device.setDeviceSn(row[0]);
        device.setDeviceAlias(row[1]);
        device.setBoxCode(row[2]);
        device.setBrandName(row[3]);
        device.setModelName(row[4]);
        device.setAppOpenCount(Integer.parseInt(row[5]));
        device.setDeviceType(row[6]);
        device.setMakerName("GSK");
        device.setDeviceLevel(row[8]);
        device.setDeviceStatus("正常使用");
        device.setInstallPlace("生产车间");
        device.setOnlineFlag("1");
        device.setDeviceCode(code);
        device.setOwnerUserId(ownerId);
        if (bound) {
            device.setTmDeptId(workshopId);
        }
        device.setTsEnterId(enterId);
        fillAudit(device, adminUserId);
    }

    private SysUser ensureNamedUser(String enterId, String mobile, String name, String mail, String roleName) {
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, mobile));
        if (exist != null) {
            return exist;
        }
        SysUser user = new SysUser();
        user.setId(UUID.randomUUID().toString());
        user.setLoginName(mobile);
        user.setLoginPassword(passwordEncoder.encode("GooCi@2026"));
        user.setDisplayName(name);
        user.setMobile(mobile);
        user.setMailAddr(mail == null || mail.isBlank() ? null : mail);
        user.setRoleName(roleName);
        user.setTsEnterId(enterId);
        fillAudit(user, user.getId());
        sysUserMapper.insert(user);
        return user;
    }

    private void bindDept(String enterId, String operatorId, String userId, String deptId) {
        Long count = userDeptRelMapper.selectCount(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, userId)
                .eq(UserDeptRel::getTmDeptId, deptId));
        if (count != null && count > 0) {
            return;
        }
        UserDeptRel rel = new UserDeptRel();
        rel.setId(UUID.randomUUID().toString());
        rel.setTmSysUserId(userId);
        rel.setTmDeptId(deptId);
        rel.setTsEnterId(enterId);
        fillAudit(rel, operatorId);
        userDeptRelMapper.insert(rel);
    }

    private void ensureStaffDemo(String enterId, String operatorId) {
        ensureDemoStaff(enterId, operatorId);
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId)
                .orderByAsc(SysUser::getCrtTime));
        int seq = 0;
        String day = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        for (SysUser user : users) {
            seq++;
            if (user.getStaffCode() == null || user.getStaffCode().isBlank()) {
                user.setStaffCode(String.format("USER%s%03d", day, seq));
                sysUserMapper.updateById(user);
            }
        }
        StaffCodeRule rule = staffCodeRuleMapper.selectOne(new LambdaQueryWrapper<StaffCodeRule>()
                .eq(StaffCodeRule::getTsEnterId, enterId));
        if (rule == null) {
            rule = new StaffCodeRule();
            rule.setId(UUID.randomUUID().toString());
            rule.setCodeName("人员编号");
            rule.setRuleJson("[{\"valueType\":\"FIXED\",\"partValue\":\"USER\"},{\"valueType\":\"DATE\",\"partValue\":\"YYMMDD\"},{\"valueType\":\"SEQ\",\"partValue\":\"001\"}]");
            rule.setSeqNo(seq);
            rule.setTsEnterId(enterId);
            fillAudit(rule, operatorId);
            staffCodeRuleMapper.insert(rule);
            return;
        }
        if (rule.getSeqNo() == null || rule.getSeqNo() < seq) {
            rule.setSeqNo(seq);
            staffCodeRuleMapper.updateById(rule);
        }
    }

    private void ensureParameter() {
        Long count = sysParameterMapper.selectCount(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        if (count != null && count > 0) {
            return;
        }
        SysParameter parameter = new SysParameter();
        parameter.setId(UUID.randomUUID().toString());
        parameter.setParamCode("SERVICE_PHONE");
        parameter.setParamValue("");
        parameter.setParamName("客服电话");
        fillAudit(parameter, "seed");
        sysParameterMapper.insert(parameter);
    }

    private void ensureDemoStaff(String enterId, String operatorId) {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId));
        if (count != null && count > 1) {
            return;
        }
        DeptRecord workshop = deptRecordMapper.selectOne(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId)
                .eq(DeptRecord::getDeptCode, "WORKSHOP"));
        String[] names = {"李工", "张工", "王工"};
        String[] mobiles = {"13900001001", "13900001002", "13900001003"};
        for (int i = 0; i < names.length; i++) {
            SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getLoginName, mobiles[i]));
            if (exist != null) {
                continue;
            }
            SysUser user = new SysUser();
            user.setId(UUID.randomUUID().toString());
            user.setLoginName(mobiles[i]);
            user.setLoginPassword(passwordEncoder.encode("GooCi@2026"));
            user.setDisplayName(names[i]);
            user.setMobile(mobiles[i]);
            user.setRoleName("员工");
            user.setTsEnterId(enterId);
            fillAudit(user, operatorId);
            sysUserMapper.insert(user);
            if (workshop != null) {
                bindDept(enterId, operatorId, user.getId(), workshop.getId());
            }
        }
    }

    private void ensureOpsUser() {
        Long count = opsUserMapper.selectCount(new LambdaQueryWrapper<OpsUser>());
        if (count != null && count > 0) {
            return;
        }
        OpsUser ops = new OpsUser();
        ops.setId(UUID.randomUUID().toString());
        ops.setLoginName("ops");
        ops.setLoginPassword(passwordEncoder.encode("GooCi@2026"));
        ops.setDisplayName("厂商运营");
        ops.setTsEnterId("OPS");
        fillAudit(ops, "seed");
        opsUserMapper.insert(ops);
    }

    private String ensureMenu(String parentId, String code, String name, String path, String icon, int sortNo) {
        SysMenu exist = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, code));
        if (exist != null) {
            return exist.getId();
        }
        return insertMenu(parentId, code, name, path, icon, sortNo);
    }

    private String insertMenu(String parentId, String code, String name, String path, String icon, int sortNo) {
        SysMenu menu = new SysMenu();
        menu.setId(UUID.randomUUID().toString());
        menu.setParentId(parentId);
        menu.setMenuCode(code);
        menu.setMenuName(name);
        menu.setRoutePath(path);
        menu.setIconCode(icon);
        menu.setSortNo(sortNo);
        fillAudit(menu, "seed");
        sysMenuMapper.insert(menu);
        return menu.getId();
    }

    private void fillAudit(com.gooci.console.common.BaseEntity entity, String userId) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCrtUsrId(userId);
        entity.setCrtUsrName("system");
        entity.setCrtTime(now);
        entity.setUpdUsrId(userId);
        entity.setUpdUsrName("system");
        entity.setUpdTime(now);
        entity.setDeleted("0");
        if (entity.getTsEnterId() == null) {
            entity.setTsEnterId(userId);
        }
    }
}
