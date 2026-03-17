package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_plan")
public class FollowUpPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private String residentName;
    private String chronicType;
    private Long doctorId;
    private String doctorName;
    private Integer frequency;
    private Integer followUpMethod;
    private LocalDate nextFollowDate;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
