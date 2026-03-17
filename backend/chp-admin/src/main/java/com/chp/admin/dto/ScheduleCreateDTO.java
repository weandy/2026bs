package com.chp.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleCreateDTO {
    @NotNull(message = "staffId不能为空")
    private Long staffId;
    @NotBlank(message = "staffName不能为空")
    private String staffName;
    @NotBlank(message = "deptCode不能为空")
    private String deptCode;
    @NotBlank(message = "deptName不能为空")
    private String deptName;
    @NotBlank(message = "scheduleDate不能为空")
    private String scheduleDate;
    private List<SlotDTO> slots;

    @Data
    public static class SlotDTO {
        private Integer timePeriod;
        private Integer totalQuota;
    }
}
