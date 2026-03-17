package com.chp.medical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionCreateDTO {
    @NotNull(message = "residentId不能为空")
    private Long residentId;
    private String notes;
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long drugId;
        private String drugName;
        private String drugSpec;
        private Integer quantity;
        private String usageMethod;
        private String dosage;
        private String frequency;
        private Integer days;
    }
}
