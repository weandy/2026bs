package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String drugCode;
    private String genericName;
    private String tradeName;
    private String spec;
    private String dosageForm;
    private String unit;
    private String pinyinCode;
    private String manufacturer;
    private Integer alertQty;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
