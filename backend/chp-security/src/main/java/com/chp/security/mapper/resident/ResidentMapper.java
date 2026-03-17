package com.chp.security.mapper.resident;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chp.security.entity.Resident;
import org.apache.ibatis.annotations.Mapper;

/**
 * 居民 Mapper（居民域数据源）
 * 放在 security.mapper.resident 子包，由 residentSqlSessionFactory 扫描
 */
@Mapper
public interface ResidentMapper extends BaseMapper<Resident> {
}
