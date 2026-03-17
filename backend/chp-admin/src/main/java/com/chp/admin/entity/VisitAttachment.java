package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visit_attachment")
public class VisitAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}
