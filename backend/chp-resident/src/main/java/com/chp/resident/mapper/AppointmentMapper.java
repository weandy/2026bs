package com.chp.resident.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chp.resident.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
