package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.DeptRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName DeptRecordMapper
 * @Description 部门数据访问
 * @author GooCi
 * @date 2026-03-23
 */
@Mapper
public interface DeptRecordMapper extends BaseMapper<DeptRecord> {
}
