package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_record")
public class FollowUpRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long residentId;
    private LocalDate followDate;
    private Integer followMethod;
    private Short systolicBp;
    private Short diastolicBp;
    private BigDecimal fastingGlucose;
    private BigDecimal postprandialGlucose;
    private Integer medicationCompliance;
    private String lifestyleIssues;
    private String healthGuidance;
    private LocalDate nextFollowDate;
    private Long staffId;
    private String staffName;
    private LocalDateTime createdAt;
}
