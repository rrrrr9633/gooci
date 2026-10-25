package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.TodoRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName TodoRecordMapper
 * @Description 待办任务
 * @author GooCi
 * @date 2026-03-26
 */
@Mapper
public interface TodoRecordMapper extends BaseMapper<TodoRecord> {
}
