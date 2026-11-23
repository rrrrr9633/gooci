package com.gooci.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.BusinessException;
import com.gooci.console.domain.SysUser;
import com.gooci.console.domain.TodoRecord;
import com.gooci.console.mapper.SysUserMapper;
import com.gooci.console.mapper.TodoRecordMapper;
import com.gooci.console.security.LoginUserHolder;
import com.gooci.console.service.TodoService;
import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.TodoItemView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TodoServiceImpl
 * @Description 待办列表。无工作流，不造任务
 * @author GooCi
 * @date 2026-03-26
 */
@Service
public class TodoServiceImpl implements TodoService {

    private static final String PENDING = "0";

    private final SysUserMapper sysUserMapper;
    private final TodoRecordMapper todoRecordMapper;

    public TodoServiceImpl(SysUserMapper sysUserMapper, TodoRecordMapper todoRecordMapper) {
        this.sysUserMapper = sysUserMapper;
        this.todoRecordMapper = todoRecordMapper;
    }

    @Override
    public SliceView<TodoItemView> load(long pageNo, long pageSize) {
        SysUser user = requireUser();
        List<TodoRecord> rows = todoRecordMapper.selectList(new LambdaQueryWrapper<TodoRecord>()
                .eq(TodoRecord::getTmSysUserId, user.getId())
                .eq(TodoRecord::getStatusFlag, PENDING)
                .orderByDesc(TodoRecord::getCrtTime));
        List<TodoItemView> views = new ArrayList<>();
        for (TodoRecord row : rows) {
            views.add(toView(row));
        }
        return slice(views, pageNo, pageSize);
    }

    @Override
    public long pendingCount() {
        String userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return 0;
        }
        Long count = todoRecordMapper.selectCount(new LambdaQueryWrapper<TodoRecord>()
                .eq(TodoRecord::getTmSysUserId, userId)
                .eq(TodoRecord::getStatusFlag, PENDING));
        return count == null ? 0 : count;
    }

    private TodoItemView toView(TodoRecord row) {
        TodoItemView view = new TodoItemView();
        view.setId(row.getId());
        view.setBizLine(nz(row.getBizLine()));
        view.setTaskType(nz(row.getTaskType()));
        view.setContent(nz(row.getContent()));
        view.setSenderName(nz(row.getSenderName()));
        view.setHandlePath(nz(row.getHandlePath()));
        view.setStatusFlag(nz(row.getStatusFlag()));
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

    private String nz(String value) {
        return value == null ? "" : value;
    }
}
