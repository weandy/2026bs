package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("schedule_transfer_request")
public class ScheduleTransferRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scheduleId;
    private Long staffId;
    private String staffName;
    private Long targetStaffId;
    private String targetStaffName;
    private String reason;
    private Integer status; // 0-待审批 1-通过 2-驳回
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
