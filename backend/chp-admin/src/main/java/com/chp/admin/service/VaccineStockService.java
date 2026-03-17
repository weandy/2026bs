package com.chp.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.VaccineStock;
import com.chp.admin.entity.VaccineStockLog;
import com.chp.admin.mapper.VaccineStockLogMapper;
import com.chp.admin.mapper.VaccineStockMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 疫苗库存管理服务
 */
@Service
@RequiredArgsConstructor
public class VaccineStockService {

    private final VaccineStockMapper vaccineStockMapper;
    private final VaccineStockLogMapper vaccineStockLogMapper;

    /** 疫苗库存列表 */
    public Page<VaccineStock> list(int page, int size, String keyword) {
        LambdaQueryWrapper<VaccineStock> q = new LambdaQueryWrapper<VaccineStock>()
                .orderByAsc(VaccineStock::getExpiryDate);
        if (keyword != null && !keyword.isBlank()) {
            q.like(VaccineStock::getVaccineName, keyword);
        }
        return vaccineStockMapper.selectPage(new Page<>(page, size), q);
    }

    /** 新增疫苗 */
    public VaccineStock create(VaccineStock stock) {
        stock.setStatus(1);
        vaccineStockMapper.insert(stock);
        return stock;
    }

    /** 更新疫苗信息 */
    public void update(Long id, VaccineStock stock) {
        stock.setId(id);
        vaccineStockMapper.updateById(stock);
    }

    /** 疫苗入库 */
    @Transactional
    public void addStock(Long vaccineId, int quantity, String batchNo) {
        VaccineStock stock = vaccineStockMapper.selectById(vaccineId);
        if (stock == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "疫苗不存在");
        }
        stock.setQuantity(stock.getQuantity() + quantity);
        if (batchNo != null) stock.setBatchNo(batchNo);
        vaccineStockMapper.updateById(stock);

        // 记录日志
        VaccineStockLog log = new VaccineStockLog();
        log.setVaccineId(vaccineId);
        log.setVaccineName(stock.getVaccineName());
        log.setBatchNo(batchNo);
        log.setOpType(1); // 入库
        log.setQuantity(quantity);
        log.setBalance(stock.getQuantity());
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUser().getName());
        log.setOpTime(LocalDateTime.now());
        vaccineStockLogMapper.insert(log);
    }

    /** 疫苗出库 */
    @Transactional
    public void removeStock(Long vaccineId, int quantity, String reason) {
        VaccineStock stock = vaccineStockMapper.selectById(vaccineId);
        if (stock == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "疫苗不存在");
        }
        if (stock.getQuantity() < quantity) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "库存不足，当前库存: " + stock.getQuantity());
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        vaccineStockMapper.updateById(stock);

        // 记录出库日志
        VaccineStockLog log = new VaccineStockLog();
        log.setVaccineId(vaccineId);
        log.setVaccineName(stock.getVaccineName());
        log.setBatchNo(stock.getBatchNo());
        log.setOpType(2); // 出库
        log.setQuantity(quantity);
        log.setBalance(stock.getQuantity());
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUser().getName());
        log.setOpTime(LocalDateTime.now());
        log.setRemark(reason);
        vaccineStockLogMapper.insert(log);
    }

    /** 效期预警（30天内过期） */
    public List<VaccineStock> expiryAlert() {
        LocalDate threshold = LocalDate.now().plusDays(30);
        return vaccineStockMapper.selectList(
                new LambdaQueryWrapper<VaccineStock>()
                        .eq(VaccineStock::getStatus, 1)
                        .le(VaccineStock::getExpiryDate, threshold)
                        .orderByAsc(VaccineStock::getExpiryDate));
    }

    /** 库存不足预警 */
    public List<VaccineStock> stockAlert() {
        return vaccineStockMapper.selectList(null).stream()
                .filter(s -> s.getStatus() == 1 && s.getQuantity() <= s.getAlertQty())
                .toList();
    }

    /** 库存操作日志 */
    public Page<VaccineStockLog> logPage(int page, int size, Long vaccineId) {
        LambdaQueryWrapper<VaccineStockLog> q = new LambdaQueryWrapper<VaccineStockLog>()
                .orderByDesc(VaccineStockLog::getOpTime);
        if (vaccineId != null) {
            q.eq(VaccineStockLog::getVaccineId, vaccineId);
        }
        return vaccineStockLogMapper.selectPage(new Page<>(page, size), q);
    }
}
