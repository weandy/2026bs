package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班主表（chp_admin.schedule）
 */
@Data
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long staffId;
    private String staffName;
    private String deptCode;
    private String deptName;
    private LocalDate scheduleDate;
    private Integer isStopped;
    private String stopReason;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
