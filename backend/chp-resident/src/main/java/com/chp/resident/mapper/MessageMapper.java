package com.chp.resident.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chp.resident.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {}
