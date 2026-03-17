package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("public_health_record")
public class PublicHealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private String residentName;
    private String serviceType;
    private LocalDate serviceDate;
    private String indicators;
    private Integer conclusion;
    private String conclusionDesc;
    private Integer referralNeeded;
    private String referralReason;
    private Long staffId;
    private String staffName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
