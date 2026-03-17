package com.chp.medical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.annotation.AuditLog;
import com.chp.common.result.Result;
import com.chp.medical.service.DispenseService;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 药房发药核对
 */
@RestController
@RequestMapping("/medical/dispense")
@RequiredArgsConstructor
public class DispenseController {

    private final DispenseService dispenseService;

    /** 待发药列表 */
    @GetMapping("/pending")
    public Result<Page<Prescription>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(dispenseService.pendingList(page, size));
    }

    /** 已发药列表 */
    @GetMapping("/dispensed")
    public Result<Page<Prescription>> dispensed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(dispenseService.dispensedList(page, size));
    }

    /** 处方明细（校对用） */
    @GetMapping("/{prescriptionId}/items")
    public Result<List<PrescriptionItem>> items(@PathVariable Long prescriptionId) {
        return Result.success(dispenseService.getItems(prescriptionId));
    }

    /** 确认发药 */
    @PostMapping("/{prescriptionId}/confirm")
    @AuditLog(type = "DISPENSE_CONFIRM", module = "dispense", description = "确认发药")
    public Result<Void> confirm(@PathVariable Long prescriptionId) {
        dispenseService.confirmDispense(prescriptionId);
        return Result.success();
    }

    /** 退回处方 */
    @PostMapping("/{prescriptionId}/reject")
    @AuditLog(type = "DISPENSE_REJECT", module = "dispense", description = "退回处方")
    public Result<Void> reject(@PathVariable Long prescriptionId, @RequestBody Map<String, String> body) {
        dispenseService.rejectDispense(prescriptionId, body.get("reason"));
        return Result.success();
    }
}
