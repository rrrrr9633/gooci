package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.DeviceField;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName DeviceFieldMapper
 * @Description 设备自定义字段
 * @author GooCi
 * @date 2026-03-24
 */
@Mapper
public interface DeviceFieldMapper extends BaseMapper<DeviceField> {
}
