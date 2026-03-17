package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.Referral;
import com.chp.admin.mapper.ReferralMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralMapper mapper;

    @PostMapping
    public Result<?> create(@RequestBody Referral referral,
                            @RequestAttribute("staffId") Long staffId) {
        referral.setFromStaffId(staffId);
        referral.setStatus(1);
        mapper.insert(referral);
        return Result.success(referral);
    }

    @GetMapping
    public Result<List<Referral>> list(@RequestAttribute("staffId") Long staffId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<Referral>()
                        .eq(Referral::getFromStaffId, staffId)
                        .orderByDesc(Referral::getCreatedAt)));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Referral r = mapper.selectById(id);
        if (r == null) return Result.error(400, "转诊单不存在");
        r.setStatus(status);
        mapper.updateById(r);
        return Result.success();
    }
}
