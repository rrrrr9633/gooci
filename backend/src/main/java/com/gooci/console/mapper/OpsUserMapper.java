package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.OpsUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName OpsUserMapper
 * @Description 厂商运营账号
 * @author GooCi
 * @date 2026-03-26
 */
@Mapper
public interface OpsUserMapper extends BaseMapper<OpsUser> {
}
