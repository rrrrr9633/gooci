package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.UserDeptRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName UserDeptRelMapper
 * @Description 用户部门关系
 * @author GooCi
 * @date 2026-03-23
 */
@Mapper
public interface UserDeptRelMapper extends BaseMapper<UserDeptRel> {
}
