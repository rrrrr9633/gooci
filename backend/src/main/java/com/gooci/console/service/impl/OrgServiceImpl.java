package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.DeviceRecord;
import com.gooci.console.domain.EnterRecord;
import com.gooci.console.domain.SysParameter;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserDeptRel;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.DeviceRecordMapper;
import com.gooci.console.mapper.EnterRecordMapper;
import com.gooci.console.mapper.SysParameterMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserDeptRelMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.OrgService;
import com.gooci.console.support.CertFileStore;
import com.gooci.console.support.SmsCodeStore;
import com.gooci.console.web.dto.AddStaffCommand;
import com.gooci.console.web.dto.AssignDeviceCommand;
import com.gooci.console.web.dto.AssignMemberCommand;
import com.gooci.console.web.dto.BankChangeView;
import com.gooci.console.web.dto.BizChangeView;
import com.gooci.console.web.dto.CertView;
import com.gooci.console.web.dto.DeptBoardView;
import com.gooci.console.web.dto.DeptIdCommand;
import com.gooci.console.web.dto.DeptNodeView;
import com.gooci.console.web.dto.DevicePoolView;
import com.gooci.console.web.dto.DeviceRowView;
import com.gooci.console.web.dto.EnterProfileView;
import com.gooci.console.web.dto.FileSavedView;
import com.gooci.console.web.dto.OrgStructureView;
import com.gooci.console.web.dto.PromoteAdminCommand;
import com.gooci.console.web.dto.RemoveDeviceCommand;
import com.gooci.console.web.dto.RemoveStaffCommand;
import com.gooci.console.web.dto.SaveBankChangeCommand;
import com.gooci.console.web.dto.SaveBizChangeCommand;
import com.gooci.console.web.dto.SaveDeptCommand;
import com.gooci.console.web.dto.SaveDeviceCommand;
import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.StaffRowView;
import com.gooci.console.web.dto.UpdateEnterCommand;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName OrgServiceImpl
 * @Description 当前企业的组织管理读写
 * @author GooCi
 * @date 2026-03-23
 */
@Service
public class OrgServiceImpl implements OrgService {

    private static final String ADMIN_ROLE = "企业管理员";
    private static final String STAFF_ROLE = "员工";
    private static final String DEFAULT_PASSWORD = "GooCi@2026";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper sysUserMapper;
    private final EnterRecordMapper enterRecordMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final UserDeptRelMapper userDeptRelMapper;
    private final DeviceRecordMapper deviceRecordMapper;
    private final SysParameterMapper sysParameterMapper;
    private final PasswordEncoder passwordEncoder;
    private final CertFileStore certFileStore;
    private final SmsCodeStore smsCodeStore;

    public OrgServiceImpl(
            SysUserMapper sysUserMapper,
            EnterRecordMapper enterRecordMapper,
            DeptRecordMapper deptRecordMapper,
            UserDeptRelMapper userDeptRelMapper,
            DeviceRecordMapper deviceRecordMapper,
            SysParameterMapper sysParameterMapper,
            PasswordEncoder passwordEncoder,
            CertFileStore certFileStore,
            SmsCodeStore smsCodeStore) {
        this.sysUserMapper = sysUserMapper;
        this.enterRecordMapper = enterRecordMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.userDeptRelMapper = userDeptRelMapper;
        this.deviceRecordMapper = deviceRecordMapper;
        this.sysParameterMapper = sysParameterMapper;
        this.passwordEncoder = passwordEncoder;
        this.certFileStore = certFileStore;
        this.smsCodeStore = smsCodeStore;
    }

    @Override
    public EnterProfileView loadEnter() {
        return toEnterView(requireEnter());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterProfileView updateEnter(UpdateEnterCommand command) {
        EnterRecord enter = requireEnter();
        String name = trimToNull(command.getEnterName());
        if (name == null) {
            throw new BusinessException("企业名称不能为空");
        }
        enterRecordMapper.update(null, new LambdaUpdateWrapper<EnterRecord>()
                .eq(EnterRecord::getId, enter.getId())
                .set(EnterRecord::getEnterName, name)
                .set(EnterRecord::getEnterNature, trimToNull(command.getEnterNature()))
                .set(EnterRecord::getFoundDate, trimToNull(command.getFoundDate()))
                .set(EnterRecord::getEnterScale, trimToNull(command.getEnterScale()))
                .set(EnterRecord::getIndustryName, trimToNull(command.getIndustryName()))
                .set(EnterRecord::getContactName, trimToNull(command.getContactName()))
                .set(EnterRecord::getContactMobile, trimToNull(command.getContactMobile()))
                .set(EnterRecord::getContactPhone, trimToNull(command.getContactPhone()))
                .set(EnterRecord::getContactMail, trimToNull(command.getContactMail()))
                .set(EnterRecord::getEnterIntro, trimToNull(command.getEnterIntro()))
                .set(EnterRecord::getProvinceName, trimToNull(command.getProvinceName()))
                .set(EnterRecord::getCityName, trimToNull(command.getCityName()))
                .set(EnterRecord::getDistrictName, trimToNull(command.getDistrictName()))
                .set(EnterRecord::getAddressText, trimToNull(command.getAddressText()))
                .set(EnterRecord::getRegionText, joinRegion(
                        command.getProvinceName(), command.getCityName(), command.getDistrictName())));
        return loadEnter();
    }

    @Override
    public OrgStructureView loadStructure() {
        EnterRecord enter = requireEnter();
        OrgStructureView view = new OrgStructureView();
        view.setEnter(toEnterView(enter));
        view.setAdmins(loadAdmins(enter.getId()));
        return view;
    }

    @Override
    public SliceView<StaffRowView> loadStaff(String deptId, String keyword, long pageNo, long pageSize) {
        EnterRecord enter = requireEnter();
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enter.getId())
                .ne(SysUser::getRoleName, ADMIN_ROLE)
                .orderByAsc(SysUser::getDisplayName));
        Set<String> inDept = null;
        if (deptId != null && !deptId.isBlank()) {
            List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                    .eq(UserDeptRel::getTmDeptId, deptId));
            inDept = new HashSet<>();
            for (UserDeptRel rel : rels) {
                inDept.add(rel.getTmSysUserId());
            }
        }
        String key = keyword == null ? "" : keyword.trim();
        List<StaffRowView> mapped = new ArrayList<>();
        for (SysUser user : users) {
            if (inDept != null && !inDept.contains(user.getId())) {
                continue;
            }
            String mobile = nz(user.getMobile());
            String name = nz(user.getDisplayName());
            if (!key.isEmpty() && !mobile.contains(key) && !name.contains(key)) {
                continue;
            }
            StaffRowView row = toStaff(user);
            row.setDeptName(resolveUserDeptNames(enter.getId(), user.getId()));
            mapped.add(row);
        }
        return slice(mapped, pageNo, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(PromoteAdminCommand command) {
        EnterRecord enter = requireEnter();
        SysUser target = sysUserMapper.selectById(command.getUserId());
        if (target == null || !enter.getId().equals(target.getTsEnterId())) {
            throw new BusinessException("人员不存在");
        }
        if (ADMIN_ROLE.equals(target.getRoleName())) {
            throw new BusinessException("已是企业管理员");
        }
        target.setRoleName(ADMIN_ROLE);
        sysUserMapper.updateById(target);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAdmin(RemoveStaffCommand command) {
        SysUser me = requireUser();
        if (me.getId().equals(command.getUserId())) {
            throw new BusinessException("不能移除当前登录账号");
        }
        EnterRecord enter = requireEnter();
        List<StaffRowView> admins = loadAdmins(enter.getId());
        if (admins.size() <= 1) {
            throw new BusinessException("至少保留一名企业管理员");
        }
        SysUser target = sysUserMapper.selectById(command.getUserId());
        if (target == null || !enter.getId().equals(target.getTsEnterId())) {
            throw new BusinessException("管理员不存在");
        }
        target.setRoleName(STAFF_ROLE);
        sysUserMapper.updateById(target);
    }

    @Override
    public CertView loadCert() {
        EnterRecord enter = requireEnter();
        CertView view = new CertView();
        if (enter.getRegisterTime() != null) {
            view.setRegisterTime(enter.getRegisterTime().format(TIME_FMT));
        } else {
            view.setRegisterTime("");
        }
        view.setBizPassed("1".equals(enter.getBizCertStatus()) ? "1" : "0");
        view.setBankPassed("1".equals(enter.getBankCertStatus()) ? "1" : "0");
        String certName = enter.getCertEnterName();
        view.setCertEnterName(certName == null || certName.isBlank() ? nz(enter.getEnterName()) : certName);
        return view;
    }

    @Override
    public BizChangeView loadBizChange() {
        return toBizView(requireEnter());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizChangeView saveBizChange(SaveBizChangeCommand command) {
        EnterRecord enter = requireEnter();
        String remark = limitRemark(command.getBizRemark());
        enterRecordMapper.update(null, new LambdaUpdateWrapper<EnterRecord>()
                .eq(EnterRecord::getId, enter.getId())
                .set(EnterRecord::getEnterName, command.getEnterName().trim())
                .set(EnterRecord::getCertEnterName, command.getEnterName().trim())
                .set(EnterRecord::getCreditCode, command.getCreditCode().trim())
                .set(EnterRecord::getLicenseType, command.getLicenseType().trim())
                .set(EnterRecord::getLicenseScan, command.getLicenseScan().trim())
                .set(EnterRecord::getLegalPerson, trimToNull(command.getLegalPerson()))
                .set(EnterRecord::getLegalIdFront, trimToNull(command.getLegalIdFront()))
                .set(EnterRecord::getLegalIdBack, trimToNull(command.getLegalIdBack()))
                .set(EnterRecord::getChangeProof, command.getChangeProof().trim())
                .set(EnterRecord::getBizRemark, remark)
                .set(EnterRecord::getBizCertStatus, "0"));
        return loadBizChange();
    }

    @Override
    public BankChangeView loadBankChange() {
        return toBankView(requireEnter());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankChangeView saveBankChange(SaveBankChangeCommand command) {
        EnterRecord enter = requireEnter();
        enterRecordMapper.update(null, new LambdaUpdateWrapper<EnterRecord>()
                .eq(EnterRecord::getId, enter.getId())
                .set(EnterRecord::getBankName, command.getBankName().trim())
                .set(EnterRecord::getBankProvince, command.getBankProvince().trim())
                .set(EnterRecord::getBankCity, command.getBankCity().trim())
                .set(EnterRecord::getBankBranch, command.getBankBranch().trim())
                .set(EnterRecord::getBankAccount, command.getBankAccount().trim())
                .set(EnterRecord::getBankPhone, command.getBankPhone().trim())
                .set(EnterRecord::getBankRemark, limitRemark(command.getBankRemark()))
                .set(EnterRecord::getBankCertStatus, "0"));
        return loadBankChange();
    }

    @Override
    public FileSavedView uploadCertFile(MultipartFile file) {
        return certFileStore.save(requireEnter().getId(), file);
    }

    @Override
    public Resource loadCertFile(String fileId) {
        return certFileStore.load(requireEnter().getId(), fileId);
    }

    @Override
    public MediaType certFileType(String fileId) {
        return certFileStore.mediaTypeOf(fileId);
    }

    @Override
    public DeptBoardView loadDeptBoard(String deptId, long devicePage, long memberPage, long pageSize) {
        EnterRecord enter = requireEnter();
        List<DeptRecord> depts = listDepts(enter.getId());
        DeptBoardView board = new DeptBoardView();
        List<DeptNodeView> tree = new ArrayList<>();
        for (DeptRecord dept : depts) {
            tree.add(toDeptNode(dept));
        }
        board.setTree(tree);
        if (depts.isEmpty()) {
            return board;
        }
        DeptRecord current = pickDept(depts, deptId);
        board.setCurrent(toDeptNode(current));
        board.setParentName(resolveParentName(depts, current.getParentId()));
        board.setDevices(sliceDevices(current.getId(), devicePage, pageSize));
        board.setMembers(sliceMembers(current.getId(), memberPage, pageSize));
        return board;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView saveDept(SaveDeptCommand command) {
        EnterRecord enter = requireEnter();
        if (command.getId() == null || command.getId().isBlank()) {
            DeptRecord dept = new DeptRecord();
            dept.setId(UUID.randomUUID().toString());
            dept.setDeptCode("D" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
            dept.setDeptName(command.getDeptName().trim());
            dept.setParentId(trimToNull(command.getParentId()));
            dept.setDeptDesc(trimToNull(command.getDeptDesc()));
            dept.setEnableFlag("1");
            dept.setSortNo(nextSort(enter.getId()));
            dept.setTsEnterId(enter.getId());
            deptRecordMapper.insert(dept);
            return loadDeptBoard(dept.getId(), 1, 1, 5);
        }
        DeptRecord dept = requireDept(enter.getId(), command.getId());
        dept.setDeptName(command.getDeptName().trim());
        dept.setParentId(trimToNull(command.getParentId()));
        dept.setDeptDesc(trimToNull(command.getDeptDesc()));
        deptRecordMapper.updateById(dept);
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView disableDept(DeptIdCommand command) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        dept.setEnableFlag("0");
        deptRecordMapper.updateById(dept);
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView enableDept(DeptIdCommand command) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        dept.setEnableFlag("1");
        deptRecordMapper.updateById(dept);
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView addDevice(SaveDeviceCommand command) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        DeviceRecord exist = deviceRecordMapper.selectOne(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getDeviceSn, command.getDeviceSn().trim()));
        if (exist != null) {
            exist.setTmDeptId(dept.getId());
            exist.setDeviceAlias(trimToNull(command.getDeviceAlias()));
            exist.setBoxCode(trimToNull(command.getBoxCode()));
            exist.setBrandName(trimToNull(command.getBrandName()));
            exist.setModelName(trimToNull(command.getModelName()));
            if (command.getAppOpenCount() != null) {
                exist.setAppOpenCount(command.getAppOpenCount());
            }
            deviceRecordMapper.updateById(exist);
            return loadDeptBoard(dept.getId(), 1, 1, 5);
        }
        DeviceRecord row = new DeviceRecord();
        row.setId(UUID.randomUUID().toString());
        row.setDeviceSn(command.getDeviceSn().trim());
        row.setDeviceAlias(trimToNull(command.getDeviceAlias()));
        row.setBoxCode(trimToNull(command.getBoxCode()));
        row.setBrandName(trimToNull(command.getBrandName()));
        row.setModelName(trimToNull(command.getModelName()));
        row.setAppOpenCount(command.getAppOpenCount() == null ? 0 : command.getAppOpenCount());
        row.setTmDeptId(dept.getId());
        row.setTsEnterId(enter.getId());
        deviceRecordMapper.insert(row);
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    public DevicePoolView loadDevicePool(String deptId) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), deptId);
        List<DeviceRecord> all = deviceRecordMapper.selectList(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getTsEnterId, enter.getId())
                .orderByAsc(DeviceRecord::getDeviceAlias)
                .orderByAsc(DeviceRecord::getDeviceSn));
        DevicePoolView pool = new DevicePoolView();
        for (DeviceRecord row : all) {
            if (dept.getId().equals(row.getTmDeptId())) {
                pool.getAssigned().add(toDeviceRow(row));
            } else {
                pool.getPending().add(toDeviceRow(row));
            }
        }
        return pool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView assignDevices(AssignDeviceCommand command) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        Set<String> keep = new HashSet<>();
        if (command.getDeviceIds() != null) {
            keep.addAll(command.getDeviceIds());
        }
        List<DeviceRecord> all = deviceRecordMapper.selectList(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getTsEnterId, enter.getId()));
        for (DeviceRecord row : all) {
            boolean want = keep.contains(row.getId());
            boolean here = dept.getId().equals(row.getTmDeptId());
            if (want && !here) {
                row.setTmDeptId(dept.getId());
                deviceRecordMapper.updateById(row);
            } else if (!want && here) {
                deviceRecordMapper.update(null, new LambdaUpdateWrapper<DeviceRecord>()
                        .eq(DeviceRecord::getId, row.getId())
                        .set(DeviceRecord::getTmDeptId, null));
            }
        }
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView removeDevice(RemoveDeviceCommand command) {
        DeviceRecord row = deviceRecordMapper.selectById(command.getId());
        if (row == null) {
            throw new BusinessException("设备不存在");
        }
        requireDept(requireEnter().getId(), row.getTmDeptId());
        String deptId = row.getTmDeptId();
        deviceRecordMapper.update(null, new LambdaUpdateWrapper<DeviceRecord>()
                .eq(DeviceRecord::getId, row.getId())
                .set(DeviceRecord::getTmDeptId, null));
        return loadDeptBoard(deptId, 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView addMember(AddStaffCommand command) {
        if (command.getDeptId() == null || command.getDeptId().isBlank()) {
            throw new BusinessException("部门不能为空");
        }
        String mobile = command.getMobile() == null ? "" : command.getMobile().trim();
        if (!mobile.matches("1\\d{10}")) {
            throw new BusinessException("请填写正确的手机号");
        }
        if (!smsCodeStore.match(mobile, command.getSmsCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        smsCodeStore.consume(mobile);
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        SysUser user = ensureUser(enter.getId(), command, STAFF_ROLE);
        Long bound = userDeptRelMapper.selectCount(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, user.getId())
                .eq(UserDeptRel::getTmDeptId, dept.getId()));
        if (bound != null && bound > 0) {
            throw new BusinessException("该成员已在部门中");
        }
        UserDeptRel rel = new UserDeptRel();
        rel.setId(UUID.randomUUID().toString());
        rel.setTmSysUserId(user.getId());
        rel.setTmDeptId(dept.getId());
        rel.setTsEnterId(enter.getId());
        userDeptRelMapper.insert(rel);
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView assignMembers(AssignMemberCommand command) {
        EnterRecord enter = requireEnter();
        DeptRecord dept = requireDept(enter.getId(), command.getDeptId());
        List<String> userIds = command.getUserIds() == null ? List.of() : command.getUserIds();
        for (String userId : userIds) {
            if (userId == null || userId.isBlank()) {
                continue;
            }
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || !enter.getId().equals(user.getTsEnterId())) {
                throw new BusinessException("人员不存在");
            }
            Long bound = userDeptRelMapper.selectCount(new LambdaQueryWrapper<UserDeptRel>()
                    .eq(UserDeptRel::getTmSysUserId, user.getId())
                    .eq(UserDeptRel::getTmDeptId, dept.getId()));
            if (bound != null && bound > 0) {
                continue;
            }
            UserDeptRel rel = new UserDeptRel();
            rel.setId(UUID.randomUUID().toString());
            rel.setTmSysUserId(user.getId());
            rel.setTmDeptId(dept.getId());
            rel.setTsEnterId(enter.getId());
            userDeptRelMapper.insert(rel);
        }
        return loadDeptBoard(dept.getId(), 1, 1, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptBoardView removeMember(RemoveStaffCommand command) {
        if (command.getDeptId() == null || command.getDeptId().isBlank()) {
            throw new BusinessException("部门不能为空");
        }
        EnterRecord enter = requireEnter();
        requireDept(enter.getId(), command.getDeptId());
        userDeptRelMapper.delete(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, command.getUserId())
                .eq(UserDeptRel::getTmDeptId, command.getDeptId()));
        return loadDeptBoard(command.getDeptId(), 1, 1, 5);
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

    private EnterRecord requireEnter() {
        SysUser user = requireUser();
        if (user.getTsEnterId() == null) {
            throw new BusinessException("未绑定企业");
        }
        EnterRecord enter = enterRecordMapper.selectById(user.getTsEnterId());
        if (enter == null) {
            throw new BusinessException("企业不存在");
        }
        return enter;
    }

    private DeptRecord requireDept(String enterId, String deptId) {
        if (deptId == null || deptId.isBlank()) {
            throw new BusinessException("部门不能为空");
        }
        DeptRecord dept = deptRecordMapper.selectById(deptId);
        if (dept == null || !enterId.equals(dept.getTsEnterId())) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    private List<DeptRecord> listDepts(String enterId) {
        return deptRecordMapper.selectList(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId)
                .orderByAsc(DeptRecord::getSortNo)
                .orderByAsc(DeptRecord::getDeptName));
    }

    private DeptRecord pickDept(List<DeptRecord> depts, String deptId) {
        if (deptId != null && !deptId.isBlank()) {
            for (DeptRecord dept : depts) {
                if (dept.getId().equals(deptId)) {
                    return dept;
                }
            }
        }
        return depts.get(0);
    }

    private String resolveParentName(List<DeptRecord> depts, String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return "无上级部门";
        }
        for (DeptRecord dept : depts) {
            if (dept.getId().equals(parentId)) {
                return nz(dept.getDeptName());
            }
        }
        return "";
    }

    private int nextSort(String enterId) {
        List<DeptRecord> depts = listDepts(enterId);
        int max = 0;
        for (DeptRecord dept : depts) {
            if (dept.getSortNo() != null && dept.getSortNo() > max) {
                max = dept.getSortNo();
            }
        }
        return max + 10;
    }

    private List<StaffRowView> loadAdmins(String enterId) {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId)
                .eq(SysUser::getRoleName, ADMIN_ROLE)
                .orderByAsc(SysUser::getDisplayName));
        List<StaffRowView> rows = new ArrayList<>();
        for (SysUser user : users) {
            rows.add(toStaff(user));
        }
        return rows;
    }

    private SliceView<DeviceRowView> sliceDevices(String deptId, long pageNo, long pageSize) {
        List<DeviceRecord> all = deviceRecordMapper.selectList(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getTmDeptId, deptId)
                .orderByAsc(DeviceRecord::getDeviceAlias)
                .orderByAsc(DeviceRecord::getDeviceSn));
        List<DeviceRowView> mapped = new ArrayList<>();
        for (DeviceRecord row : all) {
            mapped.add(toDeviceRow(row));
        }
        return slice(mapped, pageNo, pageSize);
    }

    private SliceView<StaffRowView> sliceMembers(String deptId, long pageNo, long pageSize) {
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmDeptId, deptId));
        List<StaffRowView> mapped = new ArrayList<>();
        for (UserDeptRel rel : rels) {
            SysUser user = sysUserMapper.selectById(rel.getTmSysUserId());
            if (user != null) {
                mapped.add(toStaff(user));
            }
        }
        return slice(mapped, pageNo, pageSize);
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

    private SysUser ensureUser(String enterId, AddStaffCommand command, String roleName) {
        String mobile = command.getMobile().trim();
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, mobile));
        if (exist == null) {
            exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getMobile, mobile));
        }
        if (exist != null) {
            if (exist.getTsEnterId() != null && !enterId.equals(exist.getTsEnterId())) {
                throw new BusinessException("该手机号已属于其他企业");
            }
            exist.setDisplayName(command.getDisplayName().trim());
            exist.setMobile(mobile);
            exist.setMailAddr(trimToNull(command.getMailAddr()));
            exist.setTsEnterId(enterId);
            sysUserMapper.updateById(exist);
            return exist;
        }
        SysUser user = new SysUser();
        user.setId(UUID.randomUUID().toString());
        user.setLoginName(mobile);
        user.setLoginPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setDisplayName(command.getDisplayName().trim());
        user.setMobile(mobile);
        user.setMailAddr(trimToNull(command.getMailAddr()));
        user.setRoleName(roleName);
        user.setTsEnterId(enterId);
        sysUserMapper.insert(user);
        return user;
    }

    private EnterProfileView toEnterView(EnterRecord enter) {
        EnterProfileView view = new EnterProfileView();
        view.setEnterId(enter.getId());
        view.setEnterName(nz(enter.getEnterName()));
        view.setCertStatus(nz(enter.getCertStatus()));
        view.setEnterNature(nz(enter.getEnterNature()));
        view.setFoundDate(nz(enter.getFoundDate()));
        view.setEnterScale(nz(enter.getEnterScale()));
        view.setIndustryName(nz(enter.getIndustryName()));
        view.setContactName(nz(enter.getContactName()));
        view.setContactMobile(nz(enter.getContactMobile()));
        view.setContactPhone(nz(enter.getContactPhone()));
        view.setContactMail(nz(enter.getContactMail()));
        view.setEnterIntro(nz(enter.getEnterIntro()));
        view.setProvinceName(nz(enter.getProvinceName()));
        view.setCityName(nz(enter.getCityName()));
        view.setDistrictName(nz(enter.getDistrictName()));
        view.setAddressText(nz(enter.getAddressText()));
        return view;
    }

    private DeptNodeView toDeptNode(DeptRecord dept) {
        DeptNodeView node = new DeptNodeView();
        node.setId(dept.getId());
        node.setParentId(nz(dept.getParentId()));
        node.setDeptName(nz(dept.getDeptName()));
        node.setDeptDesc(nz(dept.getDeptDesc()));
        node.setEnableFlag(dept.getEnableFlag() == null ? "1" : dept.getEnableFlag());
        return node;
    }

    private DeviceRowView toDeviceRow(DeviceRecord row) {
        DeviceRowView item = new DeviceRowView();
        item.setId(row.getId());
        item.setDeviceSn(nz(row.getDeviceSn()));
        item.setDeviceAlias(nz(row.getDeviceAlias()));
        item.setBoxCode(nz(row.getBoxCode()));
        item.setBrandName(nz(row.getBrandName()));
        item.setModelName(nz(row.getModelName()));
        item.setAppOpenCount(row.getAppOpenCount() == null ? 0 : row.getAppOpenCount());
        return item;
    }

    private StaffRowView toStaff(SysUser user) {
        StaffRowView row = new StaffRowView();
        row.setUserId(user.getId());
        row.setMobile(nz(user.getMobile()));
        row.setDisplayName(nz(user.getDisplayName()));
        row.setMailAddr(nz(user.getMailAddr()));
        return row;
    }

    private String resolveUserDeptNames(String enterId, String userId) {
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, userId));
        List<String> names = new ArrayList<>();
        for (UserDeptRel rel : rels) {
            DeptRecord dept = deptRecordMapper.selectById(rel.getTmDeptId());
            if (dept != null && enterId.equals(dept.getTsEnterId())) {
                names.add(nz(dept.getDeptName()));
            }
        }
        return String.join("、", names);
    }

    private String joinRegion(String province, String city, String district) {
        StringBuilder text = new StringBuilder();
        appendPart(text, province);
        appendPart(text, city);
        appendPart(text, district);
        return text.length() == 0 ? null : text.toString();
    }

    private void appendPart(StringBuilder text, String part) {
        String value = trimToNull(part);
        if (value == null) {
            return;
        }
        if (text.length() > 0) {
            text.append(' ');
        }
        text.append(value);
    }

    private BizChangeView toBizView(EnterRecord enter) {
        BizChangeView view = new BizChangeView();
        view.setEnterName(nz(enter.getEnterName()));
        view.setCreditCode(nz(enter.getCreditCode()));
        view.setLicenseType(nz(enter.getLicenseType()));
        view.setLicenseScan(nz(enter.getLicenseScan()));
        view.setLegalPerson(nz(enter.getLegalPerson()));
        view.setLegalIdFront(nz(enter.getLegalIdFront()));
        view.setLegalIdBack(nz(enter.getLegalIdBack()));
        view.setChangeProof(nz(enter.getChangeProof()));
        view.setBizRemark(nz(enter.getBizRemark()));
        return view;
    }

    private BankChangeView toBankView(EnterRecord enter) {
        BankChangeView view = new BankChangeView();
        view.setBankName(nz(enter.getBankName()));
        view.setBankProvince(nz(enter.getBankProvince()));
        view.setBankCity(nz(enter.getBankCity()));
        view.setBankBranch(nz(enter.getBankBranch()));
        view.setBankAccount(nz(enter.getBankAccount()));
        view.setBankPhone(nz(enter.getBankPhone()));
        view.setBankRemark(nz(enter.getBankRemark()));
        view.setServicePhone(readServicePhone());
        return view;
    }

    private String readServicePhone() {
        SysParameter parameter = sysParameterMapper.selectOne(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        if (parameter == null || parameter.getParamValue() == null) {
            return "";
        }
        return parameter.getParamValue();
    }

    private String limitRemark(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        if (text.length() > 200) {
            throw new BusinessException("备注最多200个字符");
        }
        return text;
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
