package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("drug_stock")
public class DrugStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drugId;
    private String batchNo;
    private Integer quantity;
    private LocalDate expireDate;
    private String supplier;
    private BigDecimal purchasePrice;
    private LocalDate inboundAt;
    private Long inboundBy;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
