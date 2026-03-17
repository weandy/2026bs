package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccine_appointment")
public class VaccineAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String apptNo;
    private Long residentId;
    private Long vaccineId;
    private String vaccineName;
    private Integer doseNum;
    private LocalDate apptDate;
    private Integer timePeriod;
    private String patientName;
    private String patientPhone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
