package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家庭医生签约记录
 * 状态: PENDING / ACTIVE / REJECTED / EXPIRED / CANCELLED
 */
@Data
@TableName("family_doctor_contract")
public class FamilyDoctorContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private Long doctorId;
    private String doctorName;
    private String teamName;
    private Long nurseId;
    private String nurseName;
    private String status;       // PENDING / ACTIVE / REJECTED / EXPIRED / CANCELLED
    private String applyReason;
    private String rejectReason;
    private String cancelReason;
    private Long operatorId;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String servicePackage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
