package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.SysParameter;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName SysParameterMapper
 * @Description 系统参数访问
 * @author GooCi
 * @date 2026-03-23
 */
@Mapper
public interface SysParameterMapper extends BaseMapper<SysParameter> {
}
