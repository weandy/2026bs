package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prescription_item")
public class PrescriptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String drugSpec;
    private String drugUnit;
    private Integer quantity;
    private String usageMethod;
    private String dosage;
    private String frequency;
    private Integer days;
    private String itemNotes;
    private LocalDateTime createdAt;
}
