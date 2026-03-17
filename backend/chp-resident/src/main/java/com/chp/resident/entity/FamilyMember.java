package com.chp.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family_member")
public class FamilyMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String memberName;
    private String memberPhone;
    private String relation;
    private String idCard;
    private Long linkedResidentId;
    private Integer status;
    private LocalDateTime createdAt;
}
