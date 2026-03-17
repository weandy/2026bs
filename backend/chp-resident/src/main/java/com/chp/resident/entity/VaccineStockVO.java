package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 只读 VO — 映射 vaccine_stock 表 */
@Data
@TableName("vaccine_stock")
public class VaccineStockVO {
    private Long id;
    private String vaccineCode;
    private String vaccineName;
    private String manufacturer;
    private Integer quantity;
    private Integer alertQty;
    private String expiryDate;
    private Integer status;
}
