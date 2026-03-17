package com.chp.resident.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建预约请求
 */
@Data
public class AppointmentCreateDTO {
    @NotNull(message = "出诊时段ID不能为空")
    private Long slotId;
    @NotBlank(message = "科室编码不能为空")
    private String deptCode;
    @NotBlank(message = "科室名称不能为空")
    private String deptName;
    @NotNull(message = "医生ID不能为空")
    private Long staffId;
    @NotBlank(message = "医生姓名不能为空")
    private String staffName;
    @NotNull(message = "预约日期不能为空")
    private LocalDate apptDate;
    @NotNull(message = "时段不能为空")
    private Integer timePeriod;
    @NotBlank(message = "就诊人姓名不能为空")
    private String patientName;
    @NotBlank(message = "就诊人手机号不能为空")
    private String patientPhone;
    private String symptomDesc;
}
