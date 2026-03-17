package com.chp.admin.controller;

import com.chp.admin.entity.Dept;
import com.chp.admin.mapper.DeptMapper;
import com.chp.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import com.chp.common.annotation.AuditLog;

import java.util.List;

/**
 * 科室管理接口
 */
@RestController
@RequestMapping("/admin/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptMapper deptMapper;

    @GetMapping("/list")
    @Cacheable("deptList")
    public Result<List<Dept>> list() {
        return Result.success(deptMapper.selectList(
                new LambdaQueryWrapper<Dept>().orderByAsc(Dept::getSortOrder)));
    }

    @PostMapping
    @AuditLog(type = "DEPT_CREATE", module = "dept", description = "新增科室")
    @CacheEvict(value = "deptList", allEntries = true)
    public Result<Dept> create(@RequestBody Dept dept) {
        deptMapper.insert(dept);
        return Result.success(dept);
    }

    @PutMapping("/{id}")
    @AuditLog(type = "DEPT_UPDATE", module = "dept", description = "更新科室")
    @CacheEvict(value = "deptList", allEntries = true)
    public Result<Void> update(@PathVariable Long id, @RequestBody Dept dept) {
        dept.setId(id);
        deptMapper.updateById(dept);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @AuditLog(type = "DEPT_DELETE", module = "dept", description = "删除科室")
    @CacheEvict(value = "deptList", allEntries = true)
    public Result<Void> delete(@PathVariable Long id) {
        deptMapper.deleteById(id);
        return Result.success();
    }
}
