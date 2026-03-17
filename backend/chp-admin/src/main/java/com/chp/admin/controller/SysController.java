package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.SysConfig;
import com.chp.admin.entity.Notice;
import com.chp.admin.entity.AuditLog;
import com.chp.admin.mapper.SysConfigMapper;
import com.chp.admin.mapper.NoticeMapper;
import com.chp.admin.mapper.AuditLogMapper;
import com.chp.common.result.Result;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置+公告+审计日志接口
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SysController {

    private final SysConfigMapper sysConfigMapper;
    private final NoticeMapper noticeMapper;
    private final AuditLogMapper auditLogMapper;

    // ---- 系统配置 ----
    @GetMapping("/config")
    @Cacheable("sysConfig")
    public Result<List<SysConfig>> configList() {
        return Result.success(sysConfigMapper.selectList(null));
    }

    @PutMapping("/config/{id}")
    @com.chp.common.annotation.AuditLog(type = "CONFIG_UPDATE", module = "config", description = "修改系统配置")
    @CacheEvict(value = "sysConfig", allEntries = true)
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody SysConfig config) {
        config.setId(id);
        sysConfigMapper.updateById(config);
        return Result.success();
    }

    // ---- 公告管理 ----
    @GetMapping("/notice")
    public Result<Page<Notice>> noticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreatedAt)));
    }

    @PostMapping("/notice")
    @com.chp.common.annotation.AuditLog(type = "NOTICE_CREATE", module = "notice", description = "发布公告")
    public Result<Notice> createNotice(@RequestBody Notice notice) {
        notice.setPublisherId(SecurityUtils.getCurrentUserId());
        notice.setPublisherName(SecurityUtils.getCurrentUser().getName());
        notice.setStatus(1);
        noticeMapper.insert(notice);
        return notice.getId() != null ? Result.success(notice) : Result.success();
    }

    @PutMapping("/notice/{id}")
    public Result<Void> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        noticeMapper.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/notice/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }

    // ---- 审计日志 ----
    @GetMapping("/audit-log")
    public Result<Page<AuditLog>> auditLogList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module) {
        LambdaQueryWrapper<AuditLog> q = new LambdaQueryWrapper<AuditLog>().orderByDesc(AuditLog::getOpTime);
        if (module != null) q.eq(AuditLog::getModuleCode, module);
        return Result.success(auditLogMapper.selectPage(new Page<>(page, size), q));
    }
}
