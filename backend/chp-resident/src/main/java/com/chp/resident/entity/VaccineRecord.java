package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccine_record")
public class VaccineRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private Long apptId;
    private Long vaccineId;
    private String vaccineName;
    private String batchNo;
    private Integer doseNum;
    private String injectionSite;
    private String dosage;
    private Long staffId;
    private String staffName;
    private String adverseReaction;
    private LocalDate nextDoseDate;
    private LocalDateTime vaccinatedAt;
    private LocalDateTime createdAt;
}
