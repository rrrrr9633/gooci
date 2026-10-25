package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.StaffField;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName StaffFieldMapper
 * @Description 员工自定义字段
 * @author GooCi
 * @date 2026-03-24
 */
@Mapper
public interface StaffFieldMapper extends BaseMapper<StaffField> {
}
