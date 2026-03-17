package com.chp.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.VaccineStock;
import com.chp.admin.entity.VaccineStockLog;
import com.chp.admin.service.VaccineStockService;
import com.chp.common.annotation.AuditLog;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端疫苗库存管理
 */
@RestController
@RequestMapping("/api/admin/vaccine-stock")
@RequiredArgsConstructor
public class VaccineStockController {

    private final VaccineStockService vaccineStockService;

    @GetMapping
    @Cacheable(value = "vaccineDict", key = "#page + '-' + #size + '-' + #keyword")
    public Result<Page<VaccineStock>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(vaccineStockService.list(page, size, keyword));
    }

    @PostMapping
    @AuditLog(type = "VACCINE_CREATE", module = "vaccineStock", description = "新增疫苗")
    @CacheEvict(value = "vaccineDict", allEntries = true)
    public Result<VaccineStock> create(@RequestBody VaccineStock stock) {
        return Result.success(vaccineStockService.create(stock));
    }

    @PutMapping("/{id}")
    @AuditLog(type = "VACCINE_UPDATE", module = "vaccineStock", description = "更新疫苗信息")
    @CacheEvict(value = "vaccineDict", allEntries = true)
    public Result<Void> update(@PathVariable Long id, @RequestBody VaccineStock stock) {
        vaccineStockService.update(id, stock);
        return Result.success();
    }

    @PostMapping("/{vaccineId}/add-stock")
    @AuditLog(type = "VACCINE_STOCK_IN", module = "vaccineStock", description = "疫苗入库")
    @CacheEvict(value = "vaccineDict", allEntries = true)
    public Result<Void> addStock(@PathVariable Long vaccineId, @RequestBody Map<String, Object> body) {
        int quantity = (int) body.get("quantity");
        String batchNo = (String) body.get("batchNo");
        vaccineStockService.addStock(vaccineId, quantity, batchNo);
        return Result.success();
    }

    @GetMapping("/expiry-alert")
    public Result<List<VaccineStock>> expiryAlert() {
        return Result.success(vaccineStockService.expiryAlert());
    }

    @GetMapping("/stock-alert")
    public Result<List<VaccineStock>> stockAlert() {
        return Result.success(vaccineStockService.stockAlert());
    }

    @PostMapping("/{vaccineId}/out")
    @AuditLog(type = "VACCINE_STOCK_OUT", module = "vaccineStock", description = "疫苗出库")
    @CacheEvict(value = "vaccineDict", allEntries = true)
    public Result<Void> removeStock(@PathVariable Long vaccineId, @RequestBody Map<String, Object> body) {
        int quantity = (int) body.get("quantity");
        String reason = (String) body.getOrDefault("reason", "");
        vaccineStockService.removeStock(vaccineId, quantity, reason);
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<Page<VaccineStockLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long vaccineId) {
        return Result.success(vaccineStockService.logPage(page, size, vaccineId));
    }
}
