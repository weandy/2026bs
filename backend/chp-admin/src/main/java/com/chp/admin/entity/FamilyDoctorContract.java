package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("family_doctor_contract")
public class FamilyDoctorContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private String teamName;
    private Long doctorId;
    private String doctorName;
    private Long nurseId;
    private String nurseName;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private String servicePackage;
    private Integer status;
    private LocalDateTime createdAt;
}
