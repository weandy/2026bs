package com.chp.resident.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.VaccineAppointment;
import com.chp.resident.entity.VaccineRecord;
import com.chp.resident.entity.VaccineStockVO;
import com.chp.resident.mapper.VaccineStockReadMapper;
import com.chp.resident.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resident/vaccine")
@RequiredArgsConstructor
public class VaccineController {

    private final VaccineService vaccineService;
    private final VaccineStockReadMapper vaccineStockReadMapper;

    /** 查询可接种疫苗列表（在库且有余量） */
    @GetMapping("/available")
    public Result<List<VaccineStockVO>> availableVaccines() {
        List<VaccineStockVO> list = vaccineStockReadMapper.selectList(
                new LambdaQueryWrapper<VaccineStockVO>()
                        .eq(VaccineStockVO::getStatus, 1)
                        .gt(VaccineStockVO::getQuantity, 0)
                        .orderByAsc(VaccineStockVO::getVaccineName));
        return Result.success(list);
    }

    @PostMapping("/appointment")
    public Result<VaccineAppointment> createAppt(@RequestBody VaccineAppointment appt) {
        return Result.success(vaccineService.createAppointment(appt));
    }

    @PutMapping("/appointment/{id}/cancel")
    public Result<Void> cancelAppt(@PathVariable Long id) {
        vaccineService.cancelAppointment(id);
        return Result.success();
    }

    @GetMapping("/appointments")
    public Result<Page<VaccineAppointment>> myAppts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(vaccineService.myAppointments(page, size));
    }

    @GetMapping("/records")
    public Result<List<VaccineRecord>> myRecords() {
        return Result.success(vaccineService.myRecords());
    }
}
