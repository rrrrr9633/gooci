package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.domain.InboxRecord;
import com.gooci.console.domain.SysUser;
import com.gooci.console.mapper.AppOpenMapper;
import com.gooci.console.mapper.InboxRecordMapper;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.InboxService;
import com.gooci.console.web.dto.AppOpenView;
import com.gooci.console.web.dto.InboxBoardView;
import com.gooci.console.web.dto.InboxIdsCommand;
import com.gooci.console.web.dto.InboxItemView;
import com.gooci.console.web.dto.InboxTickerView;
import com.gooci.console.web.dto.SliceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @ClassName InboxServiceImpl
 * @Description 站内信筛选、已读、删除。不自动写入
 * @author GooCi
 * @date 2026-03-26
 */
@Service
public class InboxServiceImpl implements InboxService {

    private static final String UNREAD = "0";
    private static final String READ = "1";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper sysUserMapper;
    private final InboxRecordMapper inboxRecordMapper;
    private final AppOpenMapper appOpenMapper;

    public InboxServiceImpl(
            SysUserMapper sysUserMapper,
            InboxRecordMapper inboxRecordMapper,
            AppOpenMapper appOpenMapper) {
        this.sysUserMapper = sysUserMapper;
        this.inboxRecordMapper = inboxRecordMapper;
        this.appOpenMapper = appOpenMapper;
    }

    @Override
    public InboxBoardView load(String readFlag, String rangeCode, String appCode, long pageNo, long pageSize) {
        SysUser user = requireUser();
        String flag = normalizeFlag(readFlag);
        List<InboxRecord> rows = inboxRecordMapper.selectList(query(user, flag, rangeCode, appCode)
                .orderByDesc(InboxRecord::getSendTime)
                .orderByDesc(InboxRecord::getId));
        List<InboxItemView> views = new ArrayList<>();
        for (InboxRecord row : rows) {
            views.add(toView(row));
        }
        InboxBoardView board = new InboxBoardView();
        board.setApps(openedApps(user.getTsEnterId()));
        board.setSlice(slice(views, pageNo, pageSize <= 0 ? 10 : pageSize));
        return board;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxBoardView markRead(InboxIdsCommand command) {
        List<InboxRecord> rows = ownedRows(command);
        for (InboxRecord row : rows) {
            row.setReadFlag(READ);
            inboxRecordMapper.updateById(row);
        }
        return load(UNREAD, "ALL", null, 1, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxBoardView remove(InboxIdsCommand command) {
        List<InboxRecord> rows = ownedRows(command);
        for (InboxRecord row : rows) {
            inboxRecordMapper.deleteById(row.getId());
        }
        return load(UNREAD, "ALL", null, 1, 10);
    }

    @Override
    public long unreadCount() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return 0;
        }
        Long count = inboxRecordMapper.selectCount(new LambdaQueryWrapper<InboxRecord>()
                .eq(InboxRecord::getTmSysUserId, userId)
                .eq(InboxRecord::getReadFlag, UNREAD));
        return count == null ? 0 : count;
    }

    @Override
    public List<InboxTickerView> ticker() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return List.of();
        }
        List<InboxRecord> rows = inboxRecordMapper.selectList(new LambdaQueryWrapper<InboxRecord>()
                .eq(InboxRecord::getTmSysUserId, userId)
                .eq(InboxRecord::getReadFlag, UNREAD)
                .orderByDesc(InboxRecord::getSendTime));
        List<InboxTickerView> views = new ArrayList<>();
        int cap = Math.min(8, rows.size());
        for (int i = 0; i < cap; i++) {
            InboxRecord row = rows.get(i);
            InboxTickerView view = new InboxTickerView();
            view.setId(row.getId());
            view.setContent(nz(row.getContent()));
            views.add(view);
        }
        return views;
    }

    private List<InboxRecord> ownedRows(InboxIdsCommand command) {
        SysUser user = requireUser();
        Set<String> ids = new LinkedHashSet<>();
        if (command != null && command.getIds() != null) {
            for (String id : command.getIds()) {
                if (id != null && !id.isBlank()) {
                    ids.add(id.trim());
                }
            }
        }
        if (ids.isEmpty()) {
            throw new BusinessException("请先选择要操作的消息！");
        }
        List<InboxRecord> rows = inboxRecordMapper.selectList(new LambdaQueryWrapper<InboxRecord>()
                .eq(InboxRecord::getTmSysUserId, user.getId())
                .in(InboxRecord::getId, ids));
        if (rows.isEmpty()) {
            throw new BusinessException("请先选择要操作的消息！");
        }
        return rows;
    }

    private LambdaQueryWrapper<InboxRecord> query(SysUser user, String readFlag, String rangeCode, String appCode) {
        LambdaQueryWrapper<InboxRecord> wrapper = new LambdaQueryWrapper<InboxRecord>()
                .eq(InboxRecord::getTmSysUserId, user.getId())
                .eq(InboxRecord::getReadFlag, readFlag);
        String code = trimToNull(appCode);
        if (code != null) {
            wrapper.eq(InboxRecord::getAppCode, code);
        }
        LocalDateTime from = rangeFrom(rangeCode);
        if (from != null) {
            wrapper.ge(InboxRecord::getSendTime, from);
        }
        return wrapper;
    }

    private LocalDateTime rangeFrom(String rangeCode) {
        String code = rangeCode == null ? "ALL" : rangeCode.trim().toUpperCase(Locale.ROOT);
        LocalDate today = LocalDate.now();
        if ("TODAY".equals(code)) {
            return today.atStartOfDay();
        }
        if ("3D".equals(code)) {
            return today.minusDays(2).atStartOfDay();
        }
        if ("7D".equals(code)) {
            return today.minusDays(6).atStartOfDay();
        }
        if ("30D".equals(code)) {
            return today.minusDays(29).atStartOfDay();
        }
        return null;
    }

    private String normalizeFlag(String readFlag) {
        if (READ.equals(readFlag)) {
            return READ;
        }
        return UNREAD;
    }

    private List<AppOpenView> openedApps(String enterId) {
        List<AppOpenView> apps = new ArrayList<>();
        if (enterId == null) {
            return apps;
        }
        List<AppOpen> rows = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getTsEnterId, enterId)
                .orderByAsc(AppOpen::getSortNo));
        for (AppOpen row : rows) {
            AppOpenView view = new AppOpenView();
            view.setAppCode(row.getAppCode());
            view.setAppName(row.getAppName());
            apps.add(view);
        }
        return apps;
    }

    private InboxItemView toView(InboxRecord row) {
        InboxItemView view = new InboxItemView();
        view.setId(row.getId());
        view.setContent(nz(row.getContent()));
        view.setSendTime(row.getSendTime() == null ? "" : TIME_FMT.format(row.getSendTime()));
        view.setReadFlag(nz(row.getReadFlag()));
        view.setPublisher(nz(row.getPublisher()));
        view.setAppCode(nz(row.getAppCode()));
        view.setAppName(nz(row.getAppName()));
        return view;
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
