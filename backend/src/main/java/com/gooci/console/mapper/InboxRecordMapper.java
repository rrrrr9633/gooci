package com.gooci.console.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooci.console.domain.InboxRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName InboxRecordMapper
 * @Description 站内信
 * @author GooCi
 * @date 2026-03-26
 */
@Mapper
public interface InboxRecordMapper extends BaseMapper<InboxRecord> {
}
