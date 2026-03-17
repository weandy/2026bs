package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约挂号（chp_resident.appointment）
 */
@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String apptNo;
    private Long residentId;
    private Long slotId;
    private String deptCode;
    private String deptName;
    private Long staffId;
    private String staffName;
    private LocalDate apptDate;
    private Integer timePeriod;
    private String patientName;
    private String patientPhone;
    private String symptomDesc;
    private Integer status;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
