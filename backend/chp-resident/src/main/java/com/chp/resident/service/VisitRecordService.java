package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.resident.mapper.PrescriptionMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 就诊记录服务
 */
@Service
@RequiredArgsConstructor
public class VisitRecordService {

    private final VisitRecordMapper visitRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;

    /**
     * 我的就诊记录列表
     */
    public Page<VisitRecord> getMyVisitRecords(int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return visitRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<VisitRecord>()
                        .eq(VisitRecord::getResidentId, userId)
                        .orderByDesc(VisitRecord::getCreatedAt));
    }

    /**
     * 就诊记录详情
     */
    public VisitRecord getDetail(Long id) {
        VisitRecord record = visitRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "就诊记录不存在");
        }
        return record;
    }

    /**
     * 查询某次就诊的处方列表
     */
    public List<Prescription> getPrescriptions(Long visitId) {
        return prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getVisitId, visitId));
    }

    /**
     * 查询处方明细
     */
    public List<PrescriptionItem> getPrescriptionItems(Long prescriptionId) {
        return prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>().eq(PrescriptionItem::getPrescriptionId, prescriptionId));
    }
}
