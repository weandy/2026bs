package com.chp.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 角色表（chp_admin.role）
 */
@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
}
