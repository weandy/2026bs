package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.entity.DrugStockLog;
import com.chp.admin.mapper.DrugStockLogMapper;
import com.chp.admin.mapper.DrugStockMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.resident.mapper.PrescriptionMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 药房发药核对服务
 * 业务流程：待发药列表 → 护士校对 → 确认发药 → 扣减库存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispenseService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugStockMapper drugStockMapper;
    private final DrugStockLogMapper drugStockLogMapper;

    /**
     * 待发药列表（status=1 已开方未发药）
     */
    public Page<Prescription> pendingList(int page, int size) {
        return prescriptionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, 1)
                        .orderByAsc(Prescription::getCreatedAt));
    }

    /**
     * 已发药列表（status=2 已发药）
     */
    public Page<Prescription> dispensedList(int page, int size) {
        return prescriptionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, 2)
                        .orderByDesc(Prescription::getDispensedAt));
    }

    /**
     * 获取处方明细（用于校对）
     */
    public List<PrescriptionItem> getItems(Long prescriptionId) {
        return prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
    }

    /**
     * 确认发药 — 核心事务
     * 1. 校验处方状态
     * 2. 逐项扣减药品库存
     * 3. 记录出库日志
     * 4. 更新处方状态
     */
    @Transactional
    public void confirmDispense(Long prescriptionId) {
        Prescription rx = prescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "处方不存在");
        }
        if (rx.getStatus() != 1) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "该处方已发药或已撤销");
        }

        List<PrescriptionItem> items = getItems(prescriptionId);
        if (items.isEmpty()) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "处方明细为空");
        }

        // 逐项扣减库存
        for (PrescriptionItem item : items) {
            if (item.getDrugId() == null) continue;

            // 查找可用库存（状态=1 正常）
            List<DrugStock> stocks = drugStockMapper.selectList(
                    new LambdaQueryWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, item.getDrugId())
                            .eq(DrugStock::getStatus, 1)
                            .gt(DrugStock::getQuantity, 0)
                            .orderByAsc(DrugStock::getCreatedAt)); // FIFO 先进先出

            int needQty = item.getQuantity();
            for (DrugStock stock : stocks) {
                if (needQty <= 0) break;
                int deduct = Math.min(stock.getQuantity(), needQty);
                stock.setQuantity(stock.getQuantity() - deduct);
                drugStockMapper.updateById(stock);
                needQty -= deduct;

                // 记录出库日志
                DrugStockLog logEntry = new DrugStockLog();
                logEntry.setDrugId(item.getDrugId());
                logEntry.setDrugName(item.getDrugName());
                logEntry.setDirection(2); // 出库
                logEntry.setQuantity(deduct);
                logEntry.setRemark("处方发药-" + rx.getPrescNo());
                logEntry.setOperatorName(SecurityUtils.getCurrentUser().getName());
                logEntry.setOpTime(LocalDateTime.now());
                drugStockLogMapper.insert(logEntry);
            }

            if (needQty > 0) {
                throw new BusinessException(StatusCode.DRUG_STOCK_INSUFFICIENT,
                        "药品[" + item.getDrugName() + "]库存不足，缺少" + needQty + item.getDrugUnit());
            }
        }

        // 更新处方状态
        rx.setStatus(2);
        rx.setPharmacistId(SecurityUtils.getCurrentUserId());
        rx.setPharmacistName(SecurityUtils.getCurrentUser().getName());
        rx.setDispensedAt(LocalDateTime.now());
        prescriptionMapper.updateById(rx);

        log.info("处方[{}]发药完成，药师={}", rx.getPrescNo(), rx.getPharmacistName());
    }

    /**
     * 拒绝发药（退回医生）
     */
    @Transactional
    public void rejectDispense(Long prescriptionId, String reason) {
        Prescription rx = prescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "处方不存在");
        }
        if (rx.getStatus() != 1) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "该处方非待发药状态");
        }
        rx.setStatus(3); // 3=退回
        rx.setNotes(reason);
        prescriptionMapper.updateById(rx);
        log.info("处方[{}]已退回，原因={}", rx.getPrescNo(), reason);
    }
}
