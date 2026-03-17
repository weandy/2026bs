package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation_request")
public class ConsultationRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitId;
    private Long requesterId;
    private String requesterDept;
    private String targetDeptCode;
    private String targetDeptName;
    private String reason;
    private Integer status;
    private String responseNote;
    private Long responderId;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
