package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String prescNo;
    private Long visitId;
    private Long residentId;
    private Long staffId;
    private String staffName;
    private Integer status;
    private Long pharmacistId;
    private String pharmacistName;
    private LocalDateTime dispensedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
