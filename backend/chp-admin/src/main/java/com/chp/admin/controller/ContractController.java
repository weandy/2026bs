package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.FamilyDoctorContract;
import com.chp.admin.mapper.FamilyDoctorContractMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/contract")
@RequiredArgsConstructor
public class ContractController {

    private final FamilyDoctorContractMapper mapper;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<FamilyDoctorContract> qw = new LambdaQueryWrapper<FamilyDoctorContract>()
                .eq(status != null, FamilyDoctorContract::getStatus, status)
                .orderByDesc(FamilyDoctorContract::getCreatedAt);
        return Result.success(mapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping
    public Result<?> create(@RequestBody FamilyDoctorContract contract) {
        contract.setStatus(1);
        mapper.insert(contract);
        return Result.success(contract);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyDoctorContract contract) {
        contract.setId(id);
        mapper.updateById(contract);
        return Result.success();
    }
}
