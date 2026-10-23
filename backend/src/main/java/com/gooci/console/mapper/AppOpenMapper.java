package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.AppOpen;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName AppOpenMapper
 * @Description 企业已开通 APP
 * @author GooCi
 * @date 2026-03-25
 */
@Mapper
public interface AppOpenMapper extends BaseMapper<AppOpen> {
}
