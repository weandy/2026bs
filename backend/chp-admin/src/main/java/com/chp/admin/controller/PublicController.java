package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Notice;
import com.chp.admin.mapper.NoticeMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共接口控制层 (公开访问)
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final NoticeMapper noticeMapper;

    /**
     * 获取已发布的公告列表 (无需鉴权)
     * 系统首页或居民端首页使用
     */
    @GetMapping("/notice")
    public Result<Page<Notice>> getPublicNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 仅查询 status = 1 (已发布) 的公告
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getCreatedAt);
                
        return Result.success(noticeMapper.selectPage(new Page<>(page, size), wrapper));
    }
}
