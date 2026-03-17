package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.SysDict;
import com.chp.admin.mapper.SysDictMapper;
import com.chp.common.annotation.AuditLog;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础数据字典管理
 */
@RestController
@RequestMapping("/api/admin/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper dictMapper;

    /** 按类型查询字典列表 */
    @GetMapping("/list")
    public Result<List<SysDict>> list(@RequestParam String type) {
        return Result.success(dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getType, type)
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSortOrder)));
    }

    /** 全量查询(管理用) */
    @GetMapping("/all")
    public Result<List<SysDict>> all(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<SysDict> q = new LambdaQueryWrapper<SysDict>()
                .orderByAsc(SysDict::getType)
                .orderByAsc(SysDict::getSortOrder);
        if (type != null && !type.isBlank()) {
            q.eq(SysDict::getType, type);
        }
        return Result.success(dictMapper.selectList(q));
    }

    @PostMapping
    @AuditLog(type = "DICT_CREATE", module = "dict", description = "新增字典项")
    public Result<SysDict> create(@RequestBody SysDict dict) {
        dict.setStatus(1);
        dictMapper.insert(dict);
        return Result.success(dict);
    }

    @PutMapping("/{id}")
    @AuditLog(type = "DICT_UPDATE", module = "dict", description = "更新字典项")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDict dict) {
        dict.setId(id);
        dictMapper.updateById(dict);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @AuditLog(type = "DICT_DELETE", module = "dict", description = "删除字典项")
    public Result<Void> delete(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return Result.success();
    }
}
