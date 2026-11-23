package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.DeptRecord;
import com.gooci.console.domain.DeviceCodeRule;
import com.gooci.console.domain.DeviceField;
import com.gooci.console.domain.DeviceFieldVal;
import com.gooci.console.domain.DeviceRecord;
import com.gooci.console.domain.SysUser;
import com.gooci.console.mapper.DeptRecordMapper;
import com.gooci.console.mapper.DeviceCodeRuleMapper;
import com.gooci.console.mapper.DeviceFieldMapper;
import com.gooci.console.mapper.DeviceFieldValMapper;
import com.gooci.console.mapper.DeviceRecordMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.DeviceService;
import com.gooci.console.support.StaffSheetSupport;
import com.gooci.console.web.dto.DeptNodeView;
import com.gooci.console.web.dto.DeviceAssetIdCommand;
import com.gooci.console.web.dto.DeviceBoardView;
import com.gooci.console.web.dto.DeviceColumnView;
import com.gooci.console.web.dto.DeviceExtraView;
import com.gooci.console.web.dto.DeviceFieldIdCommand;
import com.gooci.console.web.dto.DeviceFieldView;
import com.gooci.console.web.dto.DeviceHeadView;
import com.gooci.console.web.dto.DeviceImportView;
import com.gooci.console.web.dto.DeviceItemView;
import com.gooci.console.web.dto.DeviceOwnerView;
import com.gooci.console.web.dto.DeviceQuery;
import com.gooci.console.web.dto.SaveDeviceAssetCommand;
import com.gooci.console.web.dto.SaveDeviceColumnsCommand;
import com.gooci.console.web.dto.SaveDeviceFieldCommand;
import com.gooci.console.web.dto.SliceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName DeviceServiceImpl
 * @Description 当前企业的设备台账
 * @author GooCi
 * @date 2026-03-24
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final TypeReference<List<String>> KEYS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final DeviceRecordMapper deviceRecordMapper;
    private final DeviceCodeRuleMapper deviceCodeRuleMapper;
    private final DeviceFieldMapper deviceFieldMapper;
    private final DeviceFieldValMapper deviceFieldValMapper;
    private final DeptRecordMapper deptRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    public DeviceServiceImpl(
            DeviceRecordMapper deviceRecordMapper,
            DeviceCodeRuleMapper deviceCodeRuleMapper,
            DeviceFieldMapper deviceFieldMapper,
            DeviceFieldValMapper deviceFieldValMapper,
            DeptRecordMapper deptRecordMapper,
            SysUserMapper sysUserMapper,
            ObjectMapper objectMapper) {
        this.deviceRecordMapper = deviceRecordMapper;
        this.deviceCodeRuleMapper = deviceCodeRuleMapper;
        this.deviceFieldMapper = deviceFieldMapper;
        this.deviceFieldValMapper = deviceFieldValMapper;
        this.deptRecordMapper = deptRecordMapper;
        this.sysUserMapper = sysUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public DeviceBoardView loadBoard(DeviceQuery query) {
        return buildBoard(query == null ? new DeviceQuery() : query, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView syncBoard(DeviceQuery query) {
        return buildBoard(query == null ? new DeviceQuery() : query, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView saveDevice(SaveDeviceAssetCommand command) {
        String enterId = requireEnterId();
        String alias = command.getDeviceAlias().trim();
        boolean creating = command.getId() == null || command.getId().isBlank();
        DeviceRecord row;
        if (creating) {
            row = new DeviceRecord();
            row.setId(UUID.randomUUID().toString());
            row.setTsEnterId(enterId);
            row.setDeviceCode(nextCode(enterId));
            row.setAppOpenCount(0);
            fillAsset(enterId, row, command, alias);
            deviceRecordMapper.insert(row);
        } else {
            row = requireDevice(enterId, command.getId());
            fillAsset(enterId, row, command, alias);
            deviceRecordMapper.updateById(row);
        }
        replaceExtras(enterId, row.getId(), command.getExtras());
        return loadBoard(new DeviceQuery());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView removeDevice(DeviceAssetIdCommand command) {
        String enterId = requireEnterId();
        DeviceRecord row = requireDevice(enterId, command.getId());
        deviceFieldValMapper.delete(new LambdaQueryWrapper<DeviceFieldVal>()
                .eq(DeviceFieldVal::getTmDeviceId, row.getId()));
        deviceRecordMapper.deleteById(row.getId());
        return loadBoard(new DeviceQuery());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView saveColumns(SaveDeviceColumnsCommand command) {
        String enterId = requireEnterId();
        DeviceCodeRule rule = loadRule(enterId);
        List<String> keys = command.getKeys() == null ? List.of() : command.getKeys();
        rule.setColJson(writeJson(keys));
        deviceCodeRuleMapper.updateById(rule);
        return loadBoard(new DeviceQuery());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView saveField(SaveDeviceFieldCommand command) {
        String enterId = requireEnterId();
        String name = command.getFieldName().trim();
        String type = normalizeType(command.getFieldType());
        String required = "1".equals(command.getRequiredFlag()) ? "1" : "0";
        if (command.getFieldId() == null || command.getFieldId().isBlank()) {
            long count = deviceFieldMapper.selectCount(new LambdaQueryWrapper<DeviceField>()
                    .eq(DeviceField::getTsEnterId, enterId));
            if (count >= 20) {
                throw new BusinessException("自定义字段最多 20 个");
            }
            DeviceField field = new DeviceField();
            field.setId(UUID.randomUUID().toString());
            field.setFieldCode("EXT" + (count + 1));
            field.setFieldName(name);
            field.setFieldType(type);
            field.setRequiredFlag(required);
            field.setHintText(trimToNull(command.getHintText()));
            field.setSortNo((int) count + 1);
            field.setTsEnterId(enterId);
            deviceFieldMapper.insert(field);
        } else {
            DeviceField field = requireField(enterId, command.getFieldId());
            field.setFieldName(name);
            field.setFieldType(type);
            field.setRequiredFlag(required);
            field.setHintText(trimToNull(command.getHintText()));
            deviceFieldMapper.updateById(field);
        }
        return loadBoard(new DeviceQuery());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBoardView removeField(DeviceFieldIdCommand command) {
        String enterId = requireEnterId();
        DeviceField field = requireField(enterId, command.getFieldId());
        deviceFieldValMapper.delete(new LambdaQueryWrapper<DeviceFieldVal>()
                .eq(DeviceFieldVal::getTmDeviceFieldId, field.getId()));
        deviceFieldMapper.deleteById(field.getId());
        return loadBoard(new DeviceQuery());
    }

    @Override
    public byte[] templateBytes() {
        String enterId = requireEnterId();
        List<String> headers = new ArrayList<>();
        headers.add("设备编号");
        headers.add("设备序列号");
        headers.add("设备别名");
        headers.add("设备类型");
        headers.add("系统制造商");
        headers.add("系统品牌");
        headers.add("型号");
        headers.add("安装位置");
        headers.add("购置日期");
        headers.add("使用部门");
        headers.add("设备状态");
        headers.add("设备等级");
        headers.add("包机人手机号");
        headers.add("备注");
        List<DeviceField> fields = listFields(enterId);
        if (fields.isEmpty()) {
            for (int i = 1; i <= 20; i++) {
                headers.add("extend" + i);
            }
        } else {
            for (DeviceField field : fields) {
                headers.add(field.getFieldName());
            }
        }
        return StaffSheetSupport.writeTemplate(headers);
    }

    @Override
    public DeviceHeadView previewHeaders(MultipartFile file) {
        List<List<String>> table = readSheet(file);
        DeviceHeadView view = new DeviceHeadView();
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
    public DeviceImportView importDevice(MultipartFile file, String importMode, String mappingJson) {
        boolean upsert = "UPSERT".equalsIgnoreCase(importMode);
        Map<String, String> mapping = readMapping(mappingJson);
        List<List<String>> table = readSheet(file);
        if (table.isEmpty()) {
            throw new BusinessException("Excel 没有表头");
        }
        List<String> headers = table.get(0);
        String enterId = requireEnterId();
        List<DeviceField> fields = listFields(enterId);
        List<DeptRecord> depts = listDepts(enterId);
        List<SysUser> owners = listUsers(enterId);
        DeviceImportView result = new DeviceImportView();
        for (int i = 1; i < table.size(); i++) {
            int rowNo = i + 1;
            List<String> line = table.get(i);
            if (isBlankRow(line)) {
                continue;
            }
            try {
                Map<String, String> cells = zipRow(headers, line);
                applyImportRow(enterId, cells, mapping, fields, depts, owners, upsert, result);
            } catch (BusinessException ex) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getMessages().add("第" + rowNo + "行：" + ex.getMessage());
            }
        }
        return result;
    }

    private DeviceBoardView buildBoard(DeviceQuery query, boolean syncOnline) {
        String enterId = requireEnterId();
        List<DeviceField> fields = listFields(enterId);
        List<DeptRecord> depts = listDepts(enterId);
        List<SysUser> users = listUsers(enterId);
        Map<String, DeptRecord> deptMap = new HashMap<>();
        for (DeptRecord dept : depts) {
            deptMap.put(dept.getId(), dept);
        }
        Map<String, SysUser> userMap = new HashMap<>();
        for (SysUser user : users) {
            userMap.put(user.getId(), user);
        }
        List<DeviceRecord> rows = deviceRecordMapper.selectList(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getTsEnterId, enterId)
                .orderByDesc(DeviceRecord::getDeviceCode)
                .orderByDesc(DeviceRecord::getCrtTime));
        List<DeviceItemView> mapped = new ArrayList<>();
        Set<String> types = new HashSet<>();
        for (DeviceRecord row : rows) {
            if (syncOnline) {
                String online = row.getBoxCode() == null || row.getBoxCode().isBlank() ? "0" : "1";
                if (!online.equals(nz(row.getOnlineFlag()))) {
                    row.setOnlineFlag(online);
                    deviceRecordMapper.updateById(row);
                }
            }
            DeviceItemView item = toItem(row, fields, deptMap, userMap);
            if (!item.getDeviceType().isEmpty()) {
                types.add(item.getDeviceType());
            }
            if (matchItem(item, query)) {
                mapped.add(item);
            }
        }
        sortItems(mapped, query);
        DeviceBoardView board = new DeviceBoardView();
        board.setSlice(slice(mapped, query.getPageNo(), query.getPageSize()));
        board.setColumns(buildColumns(loadRule(enterId), fields));
        board.setFields(toFieldViews(fields));
        board.setDepts(toDeptViews(depts));
        board.setOwners(toOwnerViews(users));
        List<String> typeList = new ArrayList<>(types);
        typeList.sort(String::compareTo);
        board.setDeviceTypes(typeList);
        board.setNextCode(previewNext(enterId));
        return board;
    }

    private void fillAsset(String enterId, DeviceRecord row, SaveDeviceAssetCommand command, String alias) {
        String sn = trimToNull(command.getDeviceSn());
        if (sn == null) {
            sn = row.getDeviceCode();
        }
        assertSnFree(enterId, sn, row.getId());
        row.setDeviceSn(sn);
        row.setDeviceAlias(alias);
        row.setDeviceType(trimToNull(command.getDeviceType()));
        row.setMakerName(trimToNull(command.getMakerName()));
        row.setBrandName(trimToNull(command.getBrandName()));
        row.setModelName(trimToNull(command.getModelName()));
        row.setInstallPlace(trimToNull(command.getInstallPlace()));
        row.setBuyDate(trimToNull(command.getBuyDate()));
        row.setDeviceStatus(trimToNull(command.getDeviceStatus()));
        row.setDeviceLevel(trimToNull(command.getDeviceLevel()));
        row.setRemarkText(trimToNull(command.getRemarkText()));
        row.setTmDeptId(resolveDeptId(enterId, command.getDeptId()));
        row.setOwnerUserId(resolveOwnerId(enterId, command.getOwnerUserId()));
        if (row.getOnlineFlag() == null) {
            row.setOnlineFlag(row.getBoxCode() == null || row.getBoxCode().isBlank() ? "0" : "1");
        }
    }

    private void applyImportRow(
            String enterId,
            Map<String, String> cells,
            Map<String, String> mapping,
            List<DeviceField> fields,
            List<DeptRecord> depts,
            List<SysUser> owners,
            boolean upsert,
            DeviceImportView result) {
        String alias = cell(cells, mapping, "deviceAlias", "设备别名");
        if (alias.isEmpty()) {
            throw new BusinessException("设备别名必填");
        }
        String sn = cell(cells, mapping, "deviceSn", "设备序列号");
        String code = cell(cells, mapping, "deviceCode", "设备编号");
        DeviceRecord exist = findExist(enterId, sn, code);
        if (exist != null && !enterId.equals(exist.getTsEnterId())) {
            throw new BusinessException("该设备序列号已属于其他企业");
        }
        if (exist != null && !upsert) {
            result.setSkippedCount(result.getSkippedCount() + 1);
            return;
        }
        SaveDeviceAssetCommand command = new SaveDeviceAssetCommand();
        command.setDeviceAlias(alias);
        command.setDeviceSn(sn);
        command.setDeviceType(cell(cells, mapping, "deviceType", "设备类型"));
        command.setMakerName(cell(cells, mapping, "makerName", "系统制造商"));
        command.setBrandName(cell(cells, mapping, "brandName", "系统品牌"));
        command.setModelName(cell(cells, mapping, "modelName", "型号"));
        command.setInstallPlace(cell(cells, mapping, "installPlace", "安装位置"));
        command.setBuyDate(cell(cells, mapping, "buyDate", "购置日期"));
        command.setDeviceStatus(cell(cells, mapping, "deviceStatus", "设备状态"));
        command.setDeviceLevel(cell(cells, mapping, "deviceLevel", "设备等级"));
        command.setRemarkText(cell(cells, mapping, "remarkText", "备注"));
        command.setDeptId(resolveDeptName(depts, cell(cells, mapping, "deptName", "使用部门")));
        command.setOwnerUserId(resolveOwnerMobile(owners, cell(cells, mapping, "ownerMobile", "包机人手机号")));
        command.setExtras(readImportExtras(cells, mapping, fields));
        if (exist == null) {
            command.setId(null);
            saveImported(enterId, command, null, code);
            result.setCreatedCount(result.getCreatedCount() + 1);
        } else {
            command.setId(exist.getId());
            saveImported(enterId, command, exist, code);
            result.setUpdatedCount(result.getUpdatedCount() + 1);
        }
    }

    private void saveImported(String enterId, SaveDeviceAssetCommand command, DeviceRecord exist, String code) {
        DeviceRecord row = exist;
        String alias = command.getDeviceAlias().trim();
        if (row == null) {
            row = new DeviceRecord();
            row.setId(UUID.randomUUID().toString());
            row.setTsEnterId(enterId);
            row.setDeviceCode(code.isEmpty() ? nextCode(enterId) : code);
            row.setAppOpenCount(0);
            fillAsset(enterId, row, command, alias);
            deviceRecordMapper.insert(row);
        } else {
            fillAsset(enterId, row, command, alias);
            deviceRecordMapper.updateById(row);
        }
        replaceExtras(enterId, row.getId(), command.getExtras());
    }

    private List<DeviceExtraView> readImportExtras(
            Map<String, String> cells, Map<String, String> mapping, List<DeviceField> fields) {
        List<DeviceExtraView> extras = new ArrayList<>();
        int idx = 1;
        for (DeviceField field : fields) {
            String header = mapping.getOrDefault(field.getId(), field.getFieldName());
            String value = nz(cells.get(header));
            if (value.isEmpty()) {
                value = nz(cells.get("extend" + idx));
            }
            DeviceExtraView extra = new DeviceExtraView();
            extra.setFieldId(field.getId());
            extra.setFieldValue(value);
            extras.add(extra);
            idx++;
        }
        return extras;
    }

    private void replaceExtras(String enterId, String deviceId, List<DeviceExtraView> extras) {
        List<DeviceField> fields = listFields(enterId);
        Map<String, String> incoming = new HashMap<>();
        if (extras != null) {
            for (DeviceExtraView extra : extras) {
                if (extra.getFieldId() != null) {
                    incoming.put(extra.getFieldId(), extra.getFieldValue());
                }
            }
        }
        for (DeviceField field : fields) {
            String value = trimToNull(incoming.get(field.getId()));
            if ("1".equals(field.getRequiredFlag()) && value == null) {
                throw new BusinessException(field.getFieldName() + "不能为空");
            }
            if (value != null) {
                checkFieldValue(field, value);
            }
            DeviceFieldVal exist = deviceFieldValMapper.selectOne(new LambdaQueryWrapper<DeviceFieldVal>()
                    .eq(DeviceFieldVal::getTmDeviceId, deviceId)
                    .eq(DeviceFieldVal::getTmDeviceFieldId, field.getId()));
            if (exist == null) {
                if (value == null) {
                    continue;
                }
                DeviceFieldVal row = new DeviceFieldVal();
                row.setId(UUID.randomUUID().toString());
                row.setTmDeviceId(deviceId);
                row.setTmDeviceFieldId(field.getId());
                row.setFieldValue(value);
                row.setTsEnterId(enterId);
                deviceFieldValMapper.insert(row);
            } else {
                exist.setFieldValue(value);
                deviceFieldValMapper.updateById(exist);
            }
        }
    }

    private void checkFieldValue(DeviceField field, String value) {
        if ("NUMBER".equals(field.getFieldType()) && !value.matches("-?\\d+(\\.\\d+)?")) {
            throw new BusinessException(field.getFieldName() + "须为数字");
        }
    }

    private synchronized String nextCode(String enterId) {
        DeviceCodeRule rule = loadRule(enterId);
        int seq = rule.getSeqNo() == null ? 0 : rule.getSeqNo();
        seq = seq + 1;
        rule.setSeqNo(seq);
        deviceCodeRuleMapper.updateById(rule);
        return renderCode(seq);
    }

    private String previewNext(String enterId) {
        DeviceCodeRule rule = loadRule(enterId);
        int seq = rule.getSeqNo() == null ? 0 : rule.getSeqNo();
        return renderCode(seq + 1);
    }

    private String renderCode(int seq) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "EQP" + date + String.format("%04d", seq);
    }

    private DeviceCodeRule loadRule(String enterId) {
        DeviceCodeRule rule = deviceCodeRuleMapper.selectOne(new LambdaQueryWrapper<DeviceCodeRule>()
                .eq(DeviceCodeRule::getTsEnterId, enterId));
        if (rule != null) {
            return rule;
        }
        rule = new DeviceCodeRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setCodeName("设备编号");
        rule.setSeqNo(0);
        rule.setTsEnterId(enterId);
        deviceCodeRuleMapper.insert(rule);
        return rule;
    }

    private List<DeviceColumnView> buildColumns(DeviceCodeRule rule, List<DeviceField> fields) {
        List<String> visible = readKeys(rule);
        List<DeviceColumnView> columns = new ArrayList<>();
        addCol(columns, visible, "deviceCode", "设备编号", true);
        addCol(columns, visible, "deviceSn", "设备序列号", true);
        addCol(columns, visible, "deviceAlias", "设备别名", true);
        addCol(columns, visible, "deviceType", "设备类型", true);
        addCol(columns, visible, "makerName", "系统制造商", true);
        addCol(columns, visible, "brandName", "系统品牌", true);
        addCol(columns, visible, "modelName", "型号", true);
        addCol(columns, visible, "installPlace", "安装位置", true);
        addCol(columns, visible, "buyDate", "购置日期", false);
        addCol(columns, visible, "deptName", "使用部门", true);
        addCol(columns, visible, "onlineText", "是否联网", true);
        addCol(columns, visible, "deviceStatus", "设备状态", false);
        addCol(columns, visible, "deviceLevel", "设备等级", true);
        addCol(columns, visible, "ownerName", "包机人", false);
        addCol(columns, visible, "remarkText", "备注", false);
        addCol(columns, visible, "crtTime", "创建时间", false);
        for (DeviceField field : fields) {
            addCol(columns, visible, field.getId(), field.getFieldName(), false);
        }
        return columns;
    }

    private void addCol(List<DeviceColumnView> columns, List<String> visible, String key, String name, boolean on) {
        DeviceColumnView col = new DeviceColumnView();
        col.setColKey(key);
        col.setColName(name);
        boolean show = visible.isEmpty() ? on : visible.contains(key);
        col.setVisibleFlag(show ? "1" : "0");
        columns.add(col);
    }

    private List<String> readKeys(DeviceCodeRule rule) {
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

    private DeviceItemView toItem(
            DeviceRecord row,
            List<DeviceField> fields,
            Map<String, DeptRecord> deptMap,
            Map<String, SysUser> userMap) {
        DeviceItemView item = new DeviceItemView();
        item.setId(row.getId());
        item.setDeviceCode(nz(row.getDeviceCode()));
        item.setDeviceSn(nz(row.getDeviceSn()));
        item.setDeviceAlias(nz(row.getDeviceAlias()));
        item.setDeviceType(nz(row.getDeviceType()));
        item.setMakerName(nz(row.getMakerName()));
        item.setBrandName(nz(row.getBrandName()));
        item.setModelName(nz(row.getModelName()));
        item.setInstallPlace(nz(row.getInstallPlace()));
        item.setBuyDate(nz(row.getBuyDate()));
        item.setDeptId(nz(row.getTmDeptId()));
        DeptRecord dept = deptMap.get(row.getTmDeptId());
        item.setDeptName(dept == null ? "" : nz(dept.getDeptName()));
        String online = "1".equals(row.getOnlineFlag()) ? "1" : "0";
        item.setOnlineFlag(online);
        item.setOnlineText("1".equals(online) ? "是" : "否");
        item.setDeviceStatus(nz(row.getDeviceStatus()));
        item.setDeviceLevel(nz(row.getDeviceLevel()));
        item.setOwnerUserId(nz(row.getOwnerUserId()));
        SysUser owner = userMap.get(row.getOwnerUserId());
        item.setOwnerName(owner == null ? "" : nz(owner.getDisplayName()));
        item.setOwnerMobile(owner == null ? "" : nz(owner.getMobile()));
        item.setRemarkText(nz(row.getRemarkText()));
        item.setCrtTime(row.getCrtTime() == null ? "" : row.getCrtTime().format(TIME_FMT));
        List<DeviceExtraView> extras = new ArrayList<>();
        for (DeviceField field : fields) {
            DeviceFieldVal val = deviceFieldValMapper.selectOne(new LambdaQueryWrapper<DeviceFieldVal>()
                    .eq(DeviceFieldVal::getTmDeviceId, row.getId())
                    .eq(DeviceFieldVal::getTmDeviceFieldId, field.getId()));
            DeviceExtraView extra = new DeviceExtraView();
            extra.setFieldId(field.getId());
            extra.setFieldName(field.getFieldName());
            extra.setFieldValue(val == null ? "" : nz(val.getFieldValue()));
            extras.add(extra);
        }
        item.setExtras(extras);
        return item;
    }

    private boolean matchItem(DeviceItemView item, DeviceQuery query) {
        if (!contains(item.getDeviceCode(), query.getKeyword())
                && !contains(item.getDeviceSn(), query.getKeyword())
                && !contains(item.getDeviceAlias(), query.getKeyword())) {
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                return false;
            }
        }
        if (!matchCsv(item.getDeptId(), query.getDeptIds())) {
            return false;
        }
        return contains(item.getDeviceCode(), query.getDeviceCode())
                && contains(item.getDeviceSn(), query.getDeviceSn())
                && contains(item.getDeviceAlias(), query.getDeviceAlias())
                && contains(item.getDeviceType(), query.getDeviceType())
                && contains(item.getMakerName(), query.getMakerName())
                && contains(item.getBrandName(), query.getBrandName())
                && contains(item.getModelName(), query.getModelName())
                && contains(item.getInstallPlace(), query.getInstallPlace())
                && contains(item.getDeviceStatus(), query.getDeviceStatus())
                && matchCsv(item.getDeviceLevel(), query.getDeviceLevel())
                && contains(item.getOwnerName(), query.getOwnerName())
                && matchCsv(item.getOnlineFlag(), query.getOnlineFlag());
    }

    private void sortItems(List<DeviceItemView> items, DeviceQuery query) {
        String key = query.getSortKey() == null ? "" : query.getSortKey();
        boolean desc = "desc".equalsIgnoreCase(query.getSortDir());
        Comparator<DeviceItemView> cmp = switch (key) {
            case "deviceCode" -> Comparator.comparing(DeviceItemView::getDeviceCode);
            case "deviceSn" -> Comparator.comparing(DeviceItemView::getDeviceSn);
            case "deviceAlias" -> Comparator.comparing(DeviceItemView::getDeviceAlias);
            case "deviceType" -> Comparator.comparing(DeviceItemView::getDeviceType);
            case "makerName" -> Comparator.comparing(DeviceItemView::getMakerName);
            case "brandName" -> Comparator.comparing(DeviceItemView::getBrandName);
            case "modelName" -> Comparator.comparing(DeviceItemView::getModelName);
            case "installPlace" -> Comparator.comparing(DeviceItemView::getInstallPlace);
            case "buyDate" -> Comparator.comparing(DeviceItemView::getBuyDate);
            case "deptName" -> Comparator.comparing(DeviceItemView::getDeptName);
            case "onlineText" -> Comparator.comparing(DeviceItemView::getOnlineText);
            case "deviceStatus" -> Comparator.comparing(DeviceItemView::getDeviceStatus);
            case "deviceLevel" -> Comparator.comparing(DeviceItemView::getDeviceLevel);
            case "ownerName" -> Comparator.comparing(DeviceItemView::getOwnerName);
            case "remarkText" -> Comparator.comparing(DeviceItemView::getRemarkText);
            case "crtTime" -> Comparator.comparing(DeviceItemView::getCrtTime);
            default -> Comparator.comparing(DeviceItemView::getDeviceCode).reversed();
        };
        if (!key.isEmpty() && desc) {
            cmp = cmp.reversed();
        }
        items.sort(cmp);
    }

    private List<DeviceField> listFields(String enterId) {
        return deviceFieldMapper.selectList(new LambdaQueryWrapper<DeviceField>()
                .eq(DeviceField::getTsEnterId, enterId)
                .orderByAsc(DeviceField::getSortNo)
                .orderByAsc(DeviceField::getCrtTime));
    }

    private List<DeviceFieldView> toFieldViews(List<DeviceField> fields) {
        List<DeviceFieldView> views = new ArrayList<>();
        for (DeviceField field : fields) {
            DeviceFieldView view = new DeviceFieldView();
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

    private List<SysUser> listUsers(String enterId) {
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTsEnterId, enterId)
                .orderByAsc(SysUser::getDisplayName));
    }

    private List<DeviceOwnerView> toOwnerViews(List<SysUser> users) {
        List<DeviceOwnerView> views = new ArrayList<>();
        for (SysUser user : users) {
            DeviceOwnerView view = new DeviceOwnerView();
            view.setUserId(user.getId());
            view.setDisplayName(nz(user.getDisplayName()));
            view.setMobile(nz(user.getMobile()));
            views.add(view);
        }
        return views;
    }

    private DeviceRecord findExist(String enterId, String sn, String code) {
        if (sn != null && !sn.isBlank()) {
            DeviceRecord bySn = deviceRecordMapper.selectOne(new LambdaQueryWrapper<DeviceRecord>()
                    .eq(DeviceRecord::getDeviceSn, sn.trim()));
            if (bySn != null) {
                return bySn;
            }
        }
        if (code != null && !code.isBlank()) {
            return deviceRecordMapper.selectOne(new LambdaQueryWrapper<DeviceRecord>()
                    .eq(DeviceRecord::getTsEnterId, enterId)
                    .eq(DeviceRecord::getDeviceCode, code.trim()));
        }
        return null;
    }

    private void assertSnFree(String enterId, String sn, String selfId) {
        DeviceRecord hit = deviceRecordMapper.selectOne(new LambdaQueryWrapper<DeviceRecord>()
                .eq(DeviceRecord::getDeviceSn, sn));
        if (hit == null) {
            return;
        }
        if (selfId != null && selfId.equals(hit.getId())) {
            return;
        }
        if (hit.getTsEnterId() != null && !enterId.equals(hit.getTsEnterId())) {
            throw new BusinessException("该设备序列号已属于其他企业");
        }
        throw new BusinessException("该设备序列号已存在");
    }

    private String resolveDeptId(String enterId, String deptId) {
        if (deptId == null || deptId.isBlank() || "全部".equals(deptId.trim())) {
            return null;
        }
        DeptRecord dept = deptRecordMapper.selectById(deptId);
        if (dept == null || !enterId.equals(dept.getTsEnterId())) {
            throw new BusinessException("部门不存在");
        }
        return dept.getId();
    }

    private String resolveOwnerId(String enterId, String ownerId) {
        if (ownerId == null || ownerId.isBlank() || "全部".equals(ownerId.trim())) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(ownerId);
        if (user == null || !enterId.equals(user.getTsEnterId())) {
            throw new BusinessException("包机人不存在");
        }
        return user.getId();
    }

    private String resolveDeptName(List<DeptRecord> depts, String text) {
        if (text == null || text.isBlank() || "全部".equals(text.trim())) {
            return null;
        }
        String name = text.trim();
        for (DeptRecord dept : depts) {
            if (name.equals(dept.getDeptName())) {
                return dept.getId();
            }
        }
        return null;
    }

    private String resolveOwnerMobile(List<SysUser> owners, String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return null;
        }
        String key = mobile.trim();
        for (SysUser user : owners) {
            if (key.equals(user.getMobile()) || key.equals(user.getLoginName())) {
                return user.getId();
            }
        }
        return null;
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

    private DeviceField requireField(String enterId, String fieldId) {
        DeviceField field = deviceFieldMapper.selectById(fieldId);
        if (field == null || !enterId.equals(field.getTsEnterId())) {
            throw new BusinessException("字段不存在");
        }
        return field;
    }

    private DeviceRecord requireDevice(String enterId, String id) {
        DeviceRecord row = deviceRecordMapper.selectById(id);
        if (row == null || !enterId.equals(row.getTsEnterId())) {
            throw new BusinessException("设备不存在");
        }
        return row;
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

    private boolean contains(String value, String key) {
        if (key == null || key.isBlank()) {
            return true;
        }
        return nz(value).contains(key.trim());
    }

    private boolean matchCsv(String value, String csv) {
        if (csv == null || csv.isBlank()) {
            return true;
        }
        String[] bits = csv.split(",");
        for (String bit : bits) {
            if (bit.trim().equals(value)) {
                return true;
            }
        }
        return false;
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
}
