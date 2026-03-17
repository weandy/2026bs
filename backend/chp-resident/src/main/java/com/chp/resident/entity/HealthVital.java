package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_vital")
public class HealthVital {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private String vitalType;
    private String vitalValue;
    private LocalDateTime measureTime;
    private String note;
    private LocalDateTime createdAt;
}
