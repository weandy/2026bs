package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出诊时段号源（chp_admin.schedule_slot，乐观锁）
 */
@Data
@TableName("schedule_slot")
public class ScheduleSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scheduleId;
    private Integer timePeriod;
    private Integer totalQuota;
    private Integer remaining;
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
