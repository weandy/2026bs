package com.chp.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 居民账号（chp_resident.resident）
 */
@Data
@TableName("resident")
public class Resident {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String idCard;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String password;
    private String bloodType;
    private String address;
    private String avatar;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
