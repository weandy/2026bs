package com.chp.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医护/管理员账号（chp_admin.staff）
 */
@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String name;
    private String password;
    private String phone;
    private Integer gender;
    private String deptCode;
    private String deptName;
    private String jobTitle;
    private String introduction;
    private Long roleId;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
