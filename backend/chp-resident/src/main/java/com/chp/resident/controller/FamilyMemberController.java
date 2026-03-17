package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.FamilyMember;
import com.chp.resident.mapper.FamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resident/family")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberMapper mapper;

    @GetMapping
    public Result<List<FamilyMember>> list(@RequestAttribute("residentId") Long residentId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getOwnerId, residentId)
                        .eq(FamilyMember::getStatus, 1)));
    }

    @PostMapping
    public Result<?> add(@RequestBody FamilyMember member,
                         @RequestAttribute("residentId") Long residentId) {
        member.setOwnerId(residentId);
        member.setStatus(1);
        mapper.insert(member);
        return Result.success(member);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyMember member,
                            @RequestAttribute("residentId") Long residentId) {
        FamilyMember existing = mapper.selectById(id);
        if (existing == null || !existing.getOwnerId().equals(residentId))
            return Result.error(400, "无权操作");
        member.setId(id);
        member.setOwnerId(residentId);
        mapper.updateById(member);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestAttribute("residentId") Long residentId) {
        FamilyMember m = mapper.selectById(id);
        if (m == null || !m.getOwnerId().equals(residentId))
            return Result.error(400, "无权操作");
        m.setStatus(0);
        mapper.updateById(m);
        return Result.success();
    }
}
