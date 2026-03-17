package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科室
 */
@Data
@TableName("dept")
public class Dept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deptCode;
    private String deptName;
    private Boolean isApptOpen;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}
