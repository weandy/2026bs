package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.mapper.PrescriptionMapper;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
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
        }
        return rx;
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
