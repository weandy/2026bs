package com.chp.resident.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.result.Result;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.service.VisitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 就诊记录接口（居民端）
 */
@RestController
@RequestMapping("/resident/visit-record")
@RequiredArgsConstructor
public class VisitRecordController {

    private final VisitRecordService visitRecordService;

    /**
     * 我的就诊记录列表
     */
    @GetMapping
    public Result<Page<VisitRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(visitRecordService.getMyVisitRecords(page, size));
    }

    /**
     * 就诊记录详情
     */
    @GetMapping("/{id}")
    public Result<VisitRecord> detail(@PathVariable Long id) {
        return Result.success(visitRecordService.getDetail(id));
    }

    /**
     * 查看处方列表
     */
    @GetMapping("/{visitId}/prescriptions")
    public Result<List<Prescription>> prescriptions(@PathVariable Long visitId) {
        return Result.success(visitRecordService.getPrescriptions(visitId));
    }

    /**
     * 查看处方明细
     */
    @GetMapping("/prescription/{prescriptionId}/items")
    public Result<List<PrescriptionItem>> prescriptionItems(@PathVariable Long prescriptionId) {
        return Result.success(visitRecordService.getPrescriptionItems(prescriptionId));
    }
}
