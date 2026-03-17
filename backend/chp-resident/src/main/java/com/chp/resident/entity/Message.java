package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long residentId;
    private String msgType;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
