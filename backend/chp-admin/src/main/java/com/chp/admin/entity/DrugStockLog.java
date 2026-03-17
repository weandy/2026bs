package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("drug_stock_log")
public class DrugStockLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drugId;
    private String drugName;
    private String batchNo;
    private Integer opType;
    private Integer quantity;
    private Integer direction;  // 1=入库 2=出库
    private Integer balance;
    private Long relatedId;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime opTime;
    private String remark;
}
