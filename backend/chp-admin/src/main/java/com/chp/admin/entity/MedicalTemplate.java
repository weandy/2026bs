package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_template")
public class MedicalTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String category;
    private String chiefComplaint;
    private String diagnosis;
    private String treatmentPlan;
    private Long creatorId;
    private Integer isPublic;
    private LocalDateTime createdAt;
}
