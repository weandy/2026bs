package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String logType;
    private String moduleCode;
    private String action;
    private Long targetId;
    private String targetDesc;
    private String requestIp;
    private String requestUa;
    private Integer result;
    private String failReason;
    private LocalDateTime opTime;
}
