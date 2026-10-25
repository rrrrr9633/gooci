package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.StaffCodeRule;
import com.gooci.console.domain.StaffField;
import com.gooci.console.domain.StaffFieldVal;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.UserDeptRel;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.StaffCodeRuleMapper;
import com.gooci.console.mapper.StaffFieldMapper;
import com.gooci.console.mapper.StaffFieldValMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.UserDeptRelMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.StaffService;
import com.gooci.console.support.StaffSheetSupport;
import com.gooci.console.web.dto.DeptNodeView;
import com.gooci.console.web.dto.SaveStaffCodeRuleCommand;
import com.gooci.console.web.dto.SaveStaffColumnsCommand;
import com.gooci.console.web.dto.SaveStaffCommand;
import com.gooci.console.web.dto.SaveStaffFieldCommand;
import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.StaffBoardView;
import com.gooci.console.web.dto.StaffCodePartView;
import com.gooci.console.web.dto.StaffCodeRuleView;
import com.gooci.console.web.dto.StaffColumnView;
import com.gooci.console.web.dto.StaffExtraView;
import com.gooci.console.web.dto.StaffFieldIdCommand;
import com.gooci.console.web.dto.StaffFieldView;
import com.gooci.console.web.dto.StaffHeadView;
import com.gooci.console.web.dto.StaffIdCommand;
import com.gooci.console.web.dto.StaffImportView;
import com.gooci.console.web.dto.StaffItemView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName StaffServiceImpl
 * @Description 当前企业的员工管理
 * @author GooCi
 * @date 2026-03-24
 */
@Service
public class StaffServiceImpl implements StaffService {

    private static final String STAFF_ROLE = "员工";
    private static final String ADMIN_ROLE = "企业管理员";
    private static final String DEFAULT_PASSWORD = "GooCi@2026";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final TypeReference<List<StaffCodePartView>> PARTS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> KEYS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final SysUserMapper sysUserMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final UserDeptRelMapper userDeptRelMapper;
    private final StaffCodeRuleMapper staffCodeRuleMapper;
    private final StaffFieldMapper staffFieldMapper;
    private final StaffFieldValMapper staffFieldValMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public StaffServiceImpl(
            SysUserMapper sysUserMapper,
            DeptRecordMapper deptRecordMapper,
            UserDeptRelMapper userDeptRelMapper,
            StaffCodeRuleMapper staffCodeRuleMapper,
            StaffFieldMapper staffFieldMapper,
            StaffFieldValMapper staffFieldValMapper,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.userDeptRelMapper = userDeptRelMapper;
        this.staffCodeRuleMapper = staffCodeRuleMapper;
        this.staffFieldMapper = staffFieldMapper;
        this.staffFieldValMapper = staffFieldValMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public StaffBoardView loadBoard(String keyword, String deptId, long pageNo, long pageSize) {
        String enterId = requireEnterId();
        List<StaffField> fields = listFields(enterId);
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId)
                .orderByDesc(SysUser::getStaffCode)
                .orderByDesc(SysUser::getCrtTime));
        Set<String> inDept = null;
        if (deptId != null && !deptId.isBlank()) {
            List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                    .eq(UserDeptRel::getTmDeptId, deptId.trim()));
            inDept = new HashSet<>();
            for (UserDeptRel rel : rels) {
                inDept.add(rel.getTmSysUserId());
            }
        }
        String key = keyword == null ? "" : keyword.trim();
        List<StaffItemView> mapped = new ArrayList<>();
        for (SysUser user : users) {
            if (!matchUser(user, key)) {
                continue;
            }
            if (inDept != null && !inDept.contains(user.getId())) {
                continue;
            }
            mapped.add(toItem(enterId, user, fields));
        }
        StaffBoardView board = new StaffBoardView();
        board.setSlice(slice(mapped, pageNo, pageSize));
        board.setColumns(buildColumns(loadRule(enterId), fields));
        board.setFields(toFieldViews(fields));
        board.setDepts(toDeptViews(listDepts(enterId)));
        board.setCodeRule(toRuleView(loadRule(enterId)));
        return board;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffBoardView saveStaff(SaveStaffCommand command) {
        String enterId = requireEnterId();
        String mobile = command.getMobile().trim();
        if (!mobile.matches("1\\d{10}")) {
            throw new BusinessException("请填写正确的手机号");
        }
        String name = command.getDisplayName().trim();
        boolean creating = command.getUserId() == null || command.getUserId().isBlank();
        if (creating && (command.getLoginPassword() == null || command.getLoginPassword().isBlank())) {
            throw new BusinessException("请填写密码");
        }
        SysUser user;
        if (creating) {
            assertMobileFree(enterId, mobile, null);
            user = new SysUser();
            user.setId(UUID.randomUUID().toString());
            user.setLoginName(mobile);
            user.setLoginPassword(passwordEncoder.encode(command.getLoginPassword().trim()));
            user.setRoleName(STAFF_ROLE);
            user.setTsEnterId(enterId);
            user.setStaffCode(nextCode(enterId));
            fillProfile(user, mobile, name, command);
            sysUserMapper.insert(user);
        } else {
            user = requireStaff(enterId, command.getUserId());
            assertMobileFree(enterId, mobile, user.getId());
            user.setLoginName(mobile);
            if (command.getLoginPassword() != null && !command.getLoginPassword().isBlank()) {
                user.setLoginPassword(passwordEncoder.encode(command.getLoginPassword().trim()));
            }
            fillProfile(user, mobile, name, command);
            sysUserMapper.updateById(user);
        }
        replaceDepts(enterId, user.getId(), command.getDeptIds());
        replaceExtras(enterId, user.getId(), command.getExtras());
        return loadBoard(null, null, 1, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffBoardView removeStaff(StaffIdCommand command) {
        String enterId = requireEnterId();
        SysUser me = requireUser();
        if (me.getId().equals(command.getUserId())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        SysUser user = requireStaff(enterId, command.getUserId());
        if (ADMIN_ROLE.equals(user.getRoleName())) {
            long adminCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getTsEnterId, enterId)
                    .eq(SysUser::getRoleName, ADMIN_ROLE));
            if (adminCount <= 1) {
                throw new BusinessException("至少保留一名企业管理员");
            }
        }
        userDeptRelMapper.delete(new LambdaQueryWrapper<UserDeptRel>().eq(UserDeptRel::getTmSysUserId, user.getId()));
        staffFieldValMapper.delete(new LambdaQueryWrapper<StaffFieldVal>().eq(StaffFieldVal::getTmSysUserId, user.getId()));
        sysUserMapper.deleteById(user.getId());
        return loadBoard(null, null, 1, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffCodeRuleView saveCodeRule(SaveStaffCodeRuleCommand command) {
        String enterId = requireEnterId();
        List<StaffCodePartView> parts = normalizeParts(command.getParts());
        StaffCodeRule rule = loadRule(enterId);
        rule.setCodeName(command.getCodeName().trim());
        rule.setRuleJson(writeJson(parts));
        staffCodeRuleMapper.updateById(rule);
        return toRuleView(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffBoardView saveColumns(SaveStaffColumnsCommand command) {
        String enterId = requireEnterId();
        StaffCodeRule rule = loadRule(enterId);
        List<String> keys = command.getKeys() == null ? List.of() : command.getKeys();
        rule.setColJson(writeJson(keys));
        staffCodeRuleMapper.updateById(rule);
        return loadBoard(null, null, 1, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffBoardView saveField(SaveStaffFieldCommand command) {
        String enterId = requireEnterId();
        String name = command.getFieldName().trim();
        String type = normalizeType(command.getFieldType());
        String required = "1".equals(command.getRequiredFlag()) ? "1" : "0";
        if (command.getFieldId() == null || command.getFieldId().isBlank()) {
            long count = staffFieldMapper.selectCount(new LambdaQueryWrapper<StaffField>()
                    .eq(StaffField::getTsEnterId, enterId));
            if (count >= 20) {
                throw new BusinessException("自定义字段最多 20 个");
            }
            StaffField field = new StaffField();
            field.setId(UUID.randomUUID().toString());
            field.setFieldCode("EXT" + (count + 1));
            field.setFieldName(name);
            field.setFieldType(type);
            field.setRequiredFlag(required);
            field.setHintText(trimToNull(command.getHintText()));
            field.setSortNo((int) count + 1);
            field.setTsEnterId(enterId);
            staffFieldMapper.insert(field);
        } else {
            StaffField field = requireField(enterId, command.getFieldId());
            field.setFieldName(name);
            field.setFieldType(type);
            field.setRequiredFlag(required);
            field.setHintText(trimToNull(command.getHintText()));
            staffFieldMapper.updateById(field);
        }
        return loadBoard(null, null, 1, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffBoardView removeField(StaffFieldIdCommand command) {
        String enterId = requireEnterId();
        StaffField field = requireField(enterId, command.getFieldId());
        staffFieldValMapper.delete(new LambdaQueryWrapper<StaffFieldVal>()
                .eq(StaffFieldVal::getTmStaffFieldId, field.getId()));
        staffFieldMapper.deleteById(field.getId());
        return loadBoard(null, null, 1, 10);
    }

    @Override
    public byte[] templateBytes() {
        String enterId = requireEnterId();
        List<String> headers = new ArrayList<>();
        headers.add("手机号码");
        headers.add("人员名称");
        headers.add("密码");
        headers.add("部门");
        headers.add("备注");
        headers.add("电子邮箱");
        headers.add("微信");
        headers.add("钉钉");
        headers.add("员工卡号");
        List<StaffField> fields = listFields(enterId);
        if (fields.isEmpty()) {
            for (int i = 1; i <= 20; i++) {
                headers.add("extend" + i);
            }
        } else {
            for (StaffField field : fields) {
                headers.add(field.getFieldName());
            }
        }
        return StaffSheetSupport.writeTemplate(headers);
    }

    @Override
    public StaffHeadView previewHeaders(MultipartFile file) {
        List<List<String>> table = readSheet(file);
        StaffHeadView view = new StaffHeadView();
        if (!table.isEmpty()) {
            for (String header : table.get(0)) {
                String text = nz(header).trim();
                if (!text.isEmpty() && !view.getHeaders().contains(text)) {
                    view.getHeaders().add(text);
                }
            }
        }
        if (view.getHeaders().isEmpty()) {
            throw new BusinessException("Excel 没有表头");
        }
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffImportView importStaff(MultipartFile file, String importMode, String mappingJson) {
        boolean upsert = "UPSERT".equalsIgnoreCase(importMode);
        Map<String, String> mapping = readMapping(mappingJson);
        List<List<String>> table = readSheet(file);
        if (table.isEmpty()) {
            throw new BusinessException("Excel 没有表头");
        }
        List<String> headers = table.get(0);
        String enterId = requireEnterId();
        List<StaffField> fields = listFields(enterId);
        List<DeptRecord> depts = listDepts(enterId);
        StaffImportView result = new StaffImportView();
        for (int i = 1; i < table.size(); i++) {
            int rowNo = i + 1;
            List<String> line = table.get(i);
            if (isBlankRow(line)) {
                continue;
            }
            try {
                Map<String, String> cells = zipRow(headers, line);
                applyImportRow(enterId, cells, mapping, fields, depts, upsert, result);
            } catch (BusinessException ex) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getMessages().add("第" + rowNo + "行：" + ex.getMessage());
            }
        }
        return result;
    }

    private void applyImportRow(
            String enterId,
            Map<String, String> cells,
            Map<String, String> mapping,
            List<StaffField> fields,
            List<DeptRecord> depts,
            boolean upsert,
            StaffImportView result) {
        String mobile = cell(cells, mapping, "mobile", "手机号码");
        String displayName = cell(cells, mapping, "displayName", "人员名称");
        if (mobile.isEmpty() || displayName.isEmpty()) {
            throw new BusinessException("手机号码和人员名称必填");
        }
        if (!mobile.matches("1\\d{10}")) {
            throw new BusinessException("手机号不正确");
        }
        SysUser exist = findByMobile(enterId, mobile);
        if (exist != null && !enterId.equals(exist.getTsEnterId())) {
            throw new BusinessException("该手机号已属于其他企业");
        }
        if (exist != null && !upsert) {
            result.setSkippedCount(result.getSkippedCount() + 1);
            return;
        }
        SaveStaffCommand command = new SaveStaffCommand();
        command.setMobile(mobile);
        command.setDisplayName(displayName);
        command.setLoginPassword(cell(cells, mapping, "loginPassword", "密码"));
        command.setRemarkText(cell(cells, mapping, "remarkText", "备注"));
        command.setMailAddr(cell(cells, mapping, "mailAddr", "电子邮箱"));
        command.setWechatNo(cell(cells, mapping, "wechatNo", "微信"));
        command.setDingNo(cell(cells, mapping, "dingNo", "钉钉"));
        command.setCardNo(cell(cells, mapping, "cardNo", "员工卡号"));
        command.setDeptIds(resolveDeptIds(depts, cell(cells, mapping, "deptName", "部门")));
        command.setExtras(readImportExtras(cells, mapping, fields));
        if (exist == null) {
            if (command.getLoginPassword() == null || command.getLoginPassword().isBlank()) {
                command.setLoginPassword(DEFAULT_PASSWORD);
            }
            command.setUserId(null);
            saveImported(enterId, command, null);
            result.setCreatedCount(result.getCreatedCount() + 1);
        } else {
            command.setUserId(exist.getId());
            saveImported(enterId, command, exist);
            result.setUpdatedCount(result.getUpdatedCount() + 1);
        }
    }

    private void saveImported(String enterId, SaveStaffCommand command, SysUser exist) {
        SysUser user = exist;
        String mobile = command.getMobile().trim();
        String name = command.getDisplayName().trim();
        if (user == null) {
            user = new SysUser();
            user.setId(UUID.randomUUID().toString());
            user.setLoginName(mobile);
            user.setLoginPassword(passwordEncoder.encode(command.getLoginPassword().trim()));
            user.setRoleName(STAFF_ROLE);
            user.setTsEnterId(enterId);
            user.setStaffCode(nextCode(enterId));
            fillProfile(user, mobile, name, command);
            sysUserMapper.insert(user);
        } else {
            user.setLoginName(mobile);
            if (command.getLoginPassword() != null && !command.getLoginPassword().isBlank()) {
                user.setLoginPassword(passwordEncoder.encode(command.getLoginPassword().trim()));
            }
            fillProfile(user, mobile, name, command);
            sysUserMapper.updateById(user);
        }
        replaceDepts(enterId, user.getId(), command.getDeptIds());
        replaceExtras(enterId, user.getId(), command.getExtras());
    }

    private List<StaffExtraView> readImportExtras(Map<String, String> cells, Map<String, String> mapping, List<StaffField> fields) {
        List<StaffExtraView> extras = new ArrayList<>();
        int idx = 1;
        for (StaffField field : fields) {
            String header = mapping.getOrDefault(field.getId(), field.getFieldName());
            String value = nz(cells.get(header));
            if (value.isEmpty()) {
                value = nz(cells.get("extend" + idx));
            }
            StaffExtraView extra = new StaffExtraView();
            extra.setFieldId(field.getId());
            extra.setFieldValue(value);
            extras.add(extra);
            idx++;
        }
        return extras;
    }

    private void fillProfile(SysUser user, String mobile, String name, SaveStaffCommand command) {
        user.setMobile(mobile);
        user.setDisplayName(name);
        user.setRemarkText(trimToNull(command.getRemarkText()));
        user.setMailAddr(trimToNull(command.getMailAddr()));
        user.setWechatNo(trimToNull(command.getWechatNo()));
        user.setDingNo(trimToNull(command.getDingNo()));
        user.setCardNo(trimToNull(command.getCardNo()));
    }

    private void replaceDepts(String enterId, String userId, List<String> deptIds) {
        userDeptRelMapper.delete(new LambdaQueryWrapper<UserDeptRel>().eq(UserDeptRel::getTmSysUserId, userId));
        if (deptIds == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (String deptId : deptIds) {
            if (deptId == null || deptId.isBlank() || !seen.add(deptId)) {
                continue;
            }
            DeptRecord dept = deptRecordMapper.selectById(deptId);
            if (dept == null || !enterId.equals(dept.getTsEnterId())) {
                throw new BusinessException("部门不存在");
            }
            UserDeptRel rel = new UserDeptRel();
            rel.setId(UUID.randomUUID().toString());
            rel.setTmSysUserId(userId);
            rel.setTmDeptId(deptId);
            rel.setTsEnterId(enterId);
            userDeptRelMapper.insert(rel);
        }
    }

    private void replaceExtras(String enterId, String userId, List<StaffExtraView> extras) {
        List<StaffField> fields = listFields(enterId);
        Map<String, String> incoming = new HashMap<>();
        if (extras != null) {
            for (StaffExtraView extra : extras) {
                if (extra.getFieldId() != null) {
                    incoming.put(extra.getFieldId(), extra.getFieldValue());
                }
            }
        }
        for (StaffField field : fields) {
            String value = trimToNull(incoming.get(field.getId()));
            if ("1".equals(field.getRequiredFlag()) && value == null) {
                throw new BusinessException(field.getFieldName() + "不能为空");
            }
            if (value != null) {
                checkFieldValue(field, value);
            }
            StaffFieldVal exist = staffFieldValMapper.selectOne(new LambdaQueryWrapper<StaffFieldVal>()
                    .eq(StaffFieldVal::getTmSysUserId, userId)
                    .eq(StaffFieldVal::getTmStaffFieldId, field.getId()));
            if (exist == null) {
                if (value == null) {
                    continue;
                }
                StaffFieldVal row = new StaffFieldVal();
                row.setId(UUID.randomUUID().toString());
                row.setTmSysUserId(userId);
                row.setTmStaffFieldId(field.getId());
                row.setFieldValue(value);
                row.setTsEnterId(enterId);
                staffFieldValMapper.insert(row);
            } else {
                exist.setFieldValue(value);
                staffFieldValMapper.updateById(exist);
            }
        }
    }

    private void checkFieldValue(StaffField field, String value) {
        if ("NUMBER".equals(field.getFieldType()) && !value.matches("-?\\d+(\\.\\d+)?")) {
            throw new BusinessException(field.getFieldName() + "须为数字");
        }
    }

    private synchronized String nextCode(String enterId) {
        StaffCodeRule rule = loadRule(enterId);
        int seq = rule.getSeqNo() == null ? 0 : rule.getSeqNo();
        seq = seq + 1;
        rule.setSeqNo(seq);
        staffCodeRuleMapper.updateById(rule);
        return renderCode(readParts(rule), seq);
    }

    private StaffCodeRule loadRule(String enterId) {
        StaffCodeRule rule = staffCodeRuleMapper.selectOne(new LambdaQueryWrapper<StaffCodeRule>()
                .eq(StaffCodeRule::getTsEnterId, enterId));
        if (rule != null) {
            return rule;
        }
        rule = new StaffCodeRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setCodeName("人员编号");
        rule.setRuleJson(writeJson(defaultParts()));
        rule.setSeqNo(0);
        rule.setTsEnterId(enterId);
        staffCodeRuleMapper.insert(rule);
        return rule;
    }

    private StaffCodeRuleView toRuleView(StaffCodeRule rule) {
        List<StaffCodePartView> parts = readParts(rule);
        StaffCodeRuleView view = new StaffCodeRuleView();
        view.setCodeName(nz(rule.getCodeName(), "人员编号"));
        view.setParts(parts);
        int seq = rule.getSeqNo() == null ? 0 : rule.getSeqNo();
        view.setPreviewText(renderCode(parts, seq + 1));
        return view;
    }

    private List<StaffCodePartView> readParts(StaffCodeRule rule) {
        if (rule.getRuleJson() == null || rule.getRuleJson().isBlank()) {
            return defaultParts();
        }
        try {
            List<StaffCodePartView> parts = objectMapper.readValue(rule.getRuleJson(), PARTS_TYPE);
            if (parts == null || parts.isEmpty()) {
                return defaultParts();
            }
            return parts;
        } catch (Exception ex) {
            return defaultParts();
        }
    }

    private List<StaffCodePartView> defaultParts() {
        List<StaffCodePartView> parts = new ArrayList<>();
        parts.add(part("FIXED", "USER"));
        parts.add(part("DATE", "YYMMDD"));
        parts.add(part("SEQ", "001"));
        return parts;
    }

    private StaffCodePartView part(String type, String value) {
        StaffCodePartView item = new StaffCodePartView();
        item.setValueType(type);
        item.setPartValue(value);
        return item;
    }

    private List<StaffCodePartView> normalizeParts(List<StaffCodePartView> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            throw new BusinessException("请至少添加一段编码规则");
        }
        if (incoming.size() > 10) {
            throw new BusinessException("最多支持添加10行");
        }
        List<StaffCodePartView> parts = new ArrayList<>();
        for (StaffCodePartView item : incoming) {
            String type = item.getValueType() == null ? "" : item.getValueType().trim().toUpperCase();
            if (!"FIXED".equals(type) && !"DATE".equals(type) && !"SEQ".equals(type)) {
                throw new BusinessException("值类型不正确");
            }
            String value = item.getPartValue() == null ? "" : item.getPartValue().trim();
            if (value.isEmpty()) {
                throw new BusinessException("值内容不能为空");
            }
            if ("DATE".equals(type)) {
                value = value.toUpperCase();
                if (!Set.of("YYMMDD", "YYYYMMDD", "YYMM", "YYYYMM", "MMDD").contains(value)) {
                    throw new BusinessException("日期格式不正确");
                }
            }
            if ("SEQ".equals(type) && !value.matches("\\d{1,8}")) {
                throw new BusinessException("流水号须为数字");
            }
            parts.add(part(type, value));
        }
        return parts;
    }

    private String renderCode(List<StaffCodePartView> parts, int seq) {
        StringBuilder out = new StringBuilder();
        LocalDate today = LocalDate.now();
        for (StaffCodePartView part : parts) {
            String type = part.getValueType();
            String value = nz(part.getPartValue());
            if ("FIXED".equals(type)) {
                out.append(value);
            } else if ("DATE".equals(type)) {
                out.append(formatDate(today, value));
            } else if ("SEQ".equals(type)) {
                int width = Math.max(1, value.length());
                out.append(String.format("%0" + width + "d", seq));
            }
        }
        return out.toString();
    }

    private String formatDate(LocalDate day, String pattern) {
        return switch (pattern) {
            case "YYYYMMDD" -> day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            case "YYMM" -> day.format(DateTimeFormatter.ofPattern("yyMM"));
            case "YYYYMM" -> day.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case "MMDD" -> day.format(DateTimeFormatter.ofPattern("MMdd"));
            default -> day.format(DateTimeFormatter.ofPattern("yyMMdd"));
        };
    }

    private List<StaffColumnView> buildColumns(StaffCodeRule rule, List<StaffField> fields) {
        List<String> visible = readKeys(rule);
        List<StaffColumnView> columns = new ArrayList<>();
        addCol(columns, visible, "staffCode", "人员编号", true);
        addCol(columns, visible, "displayName", "人员名称", true);
        addCol(columns, visible, "mobile", "手机号码", true);
        addCol(columns, visible, "deptName", "所属部门", true);
        addCol(columns, visible, "mailAddr", "电子邮箱", true);
        addCol(columns, visible, "wechatNo", "微信号", false);
        addCol(columns, visible, "dingNo", "钉钉号", false);
        addCol(columns, visible, "remarkText", "备注", true);
        addCol(columns, visible, "crtTime", "创建时间", false);
        for (StaffField field : fields) {
            addCol(columns, visible, field.getId(), field.getFieldName(), false);
        }
        return columns;
    }

    private void addCol(List<StaffColumnView> columns, List<String> visible, String key, String name, boolean on) {
        StaffColumnView col = new StaffColumnView();
        col.setColKey(key);
        col.setColName(name);
        boolean show = visible.isEmpty() ? on : visible.contains(key);
        col.setVisibleFlag(show ? "1" : "0");
        columns.add(col);
    }

    private List<String> readKeys(StaffCodeRule rule) {
        if (rule.getColJson() == null || rule.getColJson().isBlank()) {
            return List.of();
        }
        try {
            List<String> keys = objectMapper.readValue(rule.getColJson(), KEYS_TYPE);
            return keys == null ? List.of() : keys;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<StaffField> listFields(String enterId) {
        return staffFieldMapper.selectList(new LambdaQueryWrapper<StaffField>()
                .eq(StaffField::getTsEnterId, enterId)
                .orderByAsc(StaffField::getSortNo)
                .orderByAsc(StaffField::getCrtTime));
    }

    private List<StaffFieldView> toFieldViews(List<StaffField> fields) {
        List<StaffFieldView> views = new ArrayList<>();
        for (StaffField field : fields) {
            StaffFieldView view = new StaffFieldView();
            view.setFieldId(field.getId());
            view.setFieldCode(field.getFieldCode());
            view.setFieldName(field.getFieldName());
            view.setFieldType(field.getFieldType());
            view.setRequiredFlag(field.getRequiredFlag());
            view.setHintText(nz(field.getHintText()));
            view.setSortNo(field.getSortNo());
            views.add(view);
        }
        return views;
    }

    private List<DeptRecord> listDepts(String enterId) {
        return deptRecordMapper.selectList(new LambdaQueryWrapper<DeptRecord>()
                .eq(DeptRecord::getTsEnterId, enterId)
                .eq(DeptRecord::getEnableFlag, "1")
                .orderByAsc(DeptRecord::getSortNo)
                .orderByAsc(DeptRecord::getDeptName));
    }

    private List<DeptNodeView> toDeptViews(List<DeptRecord> depts) {
        List<DeptNodeView> views = new ArrayList<>();
        for (DeptRecord dept : depts) {
            DeptNodeView node = new DeptNodeView();
            node.setId(dept.getId());
            node.setDeptName(nz(dept.getDeptName()));
            views.add(node);
        }
        return views;
    }

    private StaffItemView toItem(String enterId, SysUser user, List<StaffField> fields) {
        StaffItemView item = new StaffItemView();
        item.setUserId(user.getId());
        item.setStaffCode(nz(user.getStaffCode()));
        item.setDisplayName(nz(user.getDisplayName()));
        item.setMobile(nz(user.getMobile()));
        item.setMailAddr(nz(user.getMailAddr()));
        item.setRemarkText(nz(user.getRemarkText()));
        item.setWechatNo(nz(user.getWechatNo()));
        item.setDingNo(nz(user.getDingNo()));
        item.setCardNo(nz(user.getCardNo()));
        item.setCrtTime(user.getCrtTime() == null ? "" : user.getCrtTime().format(TIME_FMT));
        List<UserDeptRel> rels = userDeptRelMapper.selectList(new LambdaQueryWrapper<UserDeptRel>()
                .eq(UserDeptRel::getTmSysUserId, user.getId()));
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (UserDeptRel rel : rels) {
            DeptRecord dept = deptRecordMapper.selectById(rel.getTmDeptId());
            if (dept != null && enterId.equals(dept.getTsEnterId())) {
                ids.add(dept.getId());
                names.add(nz(dept.getDeptName()));
            }
        }
        item.setDeptIds(ids);
        item.setDeptName(String.join(",", names));
        List<StaffExtraView> extras = new ArrayList<>();
        for (StaffField field : fields) {
            StaffFieldVal val = staffFieldValMapper.selectOne(new LambdaQueryWrapper<StaffFieldVal>()
                    .eq(StaffFieldVal::getTmSysUserId, user.getId())
                    .eq(StaffFieldVal::getTmStaffFieldId, field.getId()));
            StaffExtraView extra = new StaffExtraView();
            extra.setFieldId(field.getId());
            extra.setFieldName(field.getFieldName());
            extra.setFieldValue(val == null ? "" : nz(val.getFieldValue()));
            extras.add(extra);
        }
        item.setExtras(extras);
        return item;
    }

    private boolean matchUser(SysUser user, String key) {
        if (key.isEmpty()) {
            return true;
        }
        return nz(user.getStaffCode()).contains(key)
                || nz(user.getDisplayName()).contains(key)
                || nz(user.getMobile()).contains(key);
    }

    private void assertMobileFree(String enterId, String mobile, String selfId) {
        SysUser byLogin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, mobile));
        SysUser byMobile = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getMobile, mobile));
        SysUser hit = byLogin != null ? byLogin : byMobile;
        if (hit == null) {
            return;
        }
        if (selfId != null && selfId.equals(hit.getId())) {
            return;
        }
        if (hit.getTsEnterId() != null && !enterId.equals(hit.getTsEnterId())) {
            throw new BusinessException("该手机号已属于其他企业");
        }
        throw new BusinessException("该手机号已存在");
    }

    private SysUser findByMobile(String enterId, String mobile) {
        SysUser byLogin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, mobile));
        if (byLogin != null) {
            return byLogin;
        }
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMobile, mobile)
                .eq(SysUser::getTsEnterId, enterId));
    }

    private List<String> resolveDeptIds(List<DeptRecord> depts, String text) {
        List<String> ids = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return ids;
        }
        String[] bits = text.split("[,，、;；/\\\\s]+");
        for (String bit : bits) {
            String name = bit.trim();
            if (name.isEmpty()) {
                continue;
            }
            for (DeptRecord dept : depts) {
                if (name.equals(dept.getDeptName()) && !ids.contains(dept.getId())) {
                    ids.add(dept.getId());
                }
            }
        }
        return ids;
    }

    private Map<String, String> zipRow(List<String> headers, List<String> line) {
        Map<String, String> cells = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i) == null ? "" : headers.get(i).trim();
            String value = i < line.size() ? nz(line.get(i)) : "";
            if (!header.isEmpty()) {
                cells.put(header, value);
            }
        }
        return cells;
    }

    private String cell(Map<String, String> cells, Map<String, String> mapping, String key, String fallback) {
        String header = mapping.getOrDefault(key, fallback);
        return nz(cells.get(header)).trim();
    }

    private List<List<String>> readSheet(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传 Excel 文件");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xlsx")) {
            throw new BusinessException("请上传 xlsx 文件");
        }
        try {
            return StaffSheetSupport.read(file.getBytes());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("无法读取 Excel 文件");
        }
    }

    private Map<String, String> readMapping(String mappingJson) {
        if (mappingJson == null || mappingJson.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, String> map = objectMapper.readValue(mappingJson, MAP_TYPE);
            return map == null ? Map.of() : map;
        } catch (Exception ex) {
            throw new BusinessException("字段映射不正确");
        }
    }

    private boolean isBlankRow(List<String> line) {
        for (String item : line) {
            if (item != null && !item.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private StaffField requireField(String enterId, String fieldId) {
        StaffField field = staffFieldMapper.selectById(fieldId);
        if (field == null || !enterId.equals(field.getTsEnterId())) {
            throw new BusinessException("字段不存在");
        }
        return field;
    }

    private SysUser requireStaff(String enterId, String userId) {
        SysUser user = sysUserMapper.selectById(userId);
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

    private String normalizeType(String type) {
        String raw = type == null ? "TEXT" : type.trim().toUpperCase();
        if ("NUMBER".equals(raw) || "DATE".equals(raw) || "TEXT".equals(raw)) {
            return raw;
        }
        return "TEXT";
    }

    private <T> SliceView<T> slice(List<T> all, long pageNo, long pageSize) {
        long size = pageSize <= 0 ? 10 : pageSize;
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

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException("无法保存配置");
        }
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

    private String nz(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
