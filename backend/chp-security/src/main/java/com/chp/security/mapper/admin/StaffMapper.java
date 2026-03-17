package com.chp.security.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chp.security.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医护/管理员 Mapper（管理域数据源）
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
