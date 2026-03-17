package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("visit_record")
public class VisitRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long residentId;
    private Long staffId;
    private String staffName;
    private String deptCode;
    private String deptName;
    private LocalDate visitDate;
    private String chiefComplaint;
    private String presentIllness;
    private String physicalExam;
    private String diagnosisCodes;
    private String diagnosisNames;
    private String medicalAdvice;
    private LocalDate revisitDate;
    private String revisitNote;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
