package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 基础数据字典
 * type: disease(疾病码) / drug(药品码) / vaccine(疫苗码)
 */
@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String code;
    private String name;
    private String parentCode;
    private Integer sortOrder;
    private Integer status; // 1=启用 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
