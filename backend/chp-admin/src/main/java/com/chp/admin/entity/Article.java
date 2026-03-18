package com.chp.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverUrl;
    private String content;
    private String summary;      // 摘要
    private String category;
    private String audience;     // 目标受众: all/resident/staff
    private String authorName;
    private Integer viewCount;
    private Integer isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
