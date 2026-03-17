package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visit_evaluation")
public class VisitEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitId;
    private Long residentId;
    private Long staffId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}
