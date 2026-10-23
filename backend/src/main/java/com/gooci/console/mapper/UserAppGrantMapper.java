package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.UserAppGrant;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName UserAppGrantMapper
 * @Description 用户应用授权
 * @author GooCi
 * @date 2026-03-23
 */
@Mapper
public interface UserAppGrantMapper extends BaseMapper<UserAppGrant> {
}
