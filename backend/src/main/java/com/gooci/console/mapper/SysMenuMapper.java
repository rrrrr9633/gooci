package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName SysMenuMapper
 * @Description 菜单数据访问
 * @author GooCi
 * @date 2026-03-23
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
