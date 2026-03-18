package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.mapper.PrescriptionMapper;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final JdbcTemplate jdbcTemplate;
    private static final AtomicLong RX_SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public Prescription createPrescription(Long visitId, Long residentId, List<PrescriptionItem> items) {
        String prescNo = "RX" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%05d", RX_SEQ.incrementAndGet() % 100000);
        Prescription rx = new Prescription();
        rx.setVisitId(visitId);
        rx.setResidentId(residentId);
        rx.setPrescNo(prescNo);
        rx.setStaffId(SecurityUtils.getCurrentUserId());
        rx.setStaffName(SecurityUtils.getCurrentUser().getName());
        rx.setStatus(1);
        prescriptionMapper.insert(rx);

        for (PrescriptionItem item : items) {
            item.setPrescriptionId(rx.getId());
            prescriptionItemMapper.insert(item);
            // 联动药品出库（跨库 JDBC，异常不回滚主事务）
            writeDrugStockLog(item, rx);
        }
        return rx;
    }

    /** 向 chp_admin.drug_stock_log 写入出库记录 */
    private void writeDrugStockLog(PrescriptionItem item, Prescription rx) {
        try {
            if (item.getDrugId() == null) return;
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            String staffName = SecurityUtils.getCurrentUser() != null ? SecurityUtils.getCurrentUser().getName() : "医生";
            // direction=2 表示出库
            jdbcTemplate.update(
                "INSERT INTO chp_admin.drug_stock_log (drug_id, drug_name, direction, quantity, remark, op_time) " +
                "VALUES (?, ?, 2, ?, ?, ?)",
                item.getDrugId(),
                item.getDrugName() != null ? item.getDrugName() : "",
                qty,
                "处方出库: " + rx.getPrescNo() + " [医生:" + staffName + "]",
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("处方出库日志写入失败: drugId={}, prescNo={}", item.getDrugId(), rx.getPrescNo(), e);
        }
    }

    public List<Prescription> getByVisitId(Long visitId) {
        return prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getVisitId, visitId));
    }

    public List<PrescriptionItem> getItems(Long prescriptionId) {
        return prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>().eq(PrescriptionItem::getPrescriptionId, prescriptionId));
    }
}
