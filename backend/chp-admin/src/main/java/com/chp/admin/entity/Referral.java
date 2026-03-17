package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("referral")
public class Referral {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitId;
    private Long residentId;
    private Long fromStaffId;
    private String fromDeptCode;
    private String toHospital;
    private String toDept;
    private String reason;
    private String diagnosis;
    private Integer status;
    private LocalDateTime createdAt;
}
