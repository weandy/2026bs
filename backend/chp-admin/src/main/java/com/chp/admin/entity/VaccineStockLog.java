package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vaccine_stock_log")
public class VaccineStockLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long vaccineId;
    private String vaccineName;
    private String batchNo;
    private Integer opType;  // 1-入库 2-出库 3-报损
    private Integer quantity;
    private Integer balance;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime opTime;
    private String remark;
}
