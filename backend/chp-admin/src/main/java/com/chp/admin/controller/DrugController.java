package com.chp.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Drug;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.service.DrugService;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import com.chp.common.annotation.AuditLog;

import java.util.List;

/**
 * 药品库存管理接口
 */
@RestController
@RequestMapping("/admin/drug")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping
    @Cacheable(value = "drugDict", key = "#page + '-' + #size + '-' + #keyword")
    public Result<Page<Drug>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(drugService.drugList(page, size, keyword));
    }

    @PostMapping
    @AuditLog(type = "DRUG_CREATE", module = "drug", description = "新增药品")
    @CacheEvict(value = "drugDict", allEntries = true)
    public Result<Drug> create(@RequestBody Drug drug) {
        return Result.success(drugService.createDrug(drug));
    }

    @PutMapping("/{id}")
    @AuditLog(type = "DRUG_UPDATE", module = "drug", description = "更新药品")
    @CacheEvict(value = "drugDict", allEntries = true)
    public Result<Void> update(@PathVariable Long id, @RequestBody Drug drug) {
        drug.setId(id);
        drugService.updateDrug(drug);
        return Result.success();
    }

    @GetMapping("/{drugId}/stock")
    public Result<List<DrugStock>> stock(@PathVariable Long drugId) {
        return Result.success(drugService.stockByDrug(drugId));
    }

    @PostMapping("/stock")
    @AuditLog(type = "STOCK_ADD", module = "drug", description = "入库")
    @CacheEvict(value = "drugDict", allEntries = true)
    public Result<DrugStock> addStock(@RequestBody DrugStock stock) {
        return Result.success(drugService.addStock(stock));
    }

    /** GET /api/admin/drug/stock-logs?page=1&size=20&keyword=&changeType= */
    @GetMapping("/stock-logs")
    public Result<?> stockLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer changeType) {
        return Result.success(drugService.stockLogs(page, size, keyword, changeType));
    }

    /** GET /api/admin/drug/expiry-board — 效期看板 */
    @GetMapping("/expiry-board")
    public Result<?> expiryBoard() {
        return Result.success(drugService.expiryBoard());
    }
}
