package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 疫苗库存主表
 */
@Data
@TableName("vaccine_stock")
public class VaccineStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String vaccineName;
    private String vaccineCode;
    private String manufacturer;
    private String batchNo;
    private LocalDate expiryDate;
    private Integer quantity;
    private Integer alertQty;
    private Integer status; // 1-正常 0-停用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
