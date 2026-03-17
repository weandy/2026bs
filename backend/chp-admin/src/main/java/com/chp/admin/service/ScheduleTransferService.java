package com.chp.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.ScheduleTransferRequest;
import com.chp.admin.mapper.ScheduleTransferRequestMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScheduleTransferService {

    private final ScheduleTransferRequestMapper transferMapper;

    /** 提交调班申请 */
    public void submit(ScheduleTransferRequest req) {
        req.setStaffId(SecurityUtils.getCurrentUserId());
        req.setStaffName(SecurityUtils.getCurrentUser().getName());
        req.setStatus(0);
        transferMapper.insert(req);
    }

    /** 审批 */
    public void review(Long id, Integer status, String comment) {
        ScheduleTransferRequest req = transferMapper.selectById(id);
        if (req == null) throw new BusinessException(StatusCode.NOT_FOUND, "申请不存在");
        if (req.getStatus() != 0) throw new BusinessException(StatusCode.BAD_REQUEST, "该申请已审批");
        req.setStatus(status);
        req.setReviewComment(comment);
        req.setReviewerId(SecurityUtils.getCurrentUserId());
        req.setReviewerName(SecurityUtils.getCurrentUser().getName());
        req.setReviewedAt(LocalDateTime.now());
        transferMapper.updateById(req);
    }

    /** 分页列表 */
    public IPage<ScheduleTransferRequest> list(int page, int size, Integer status) {
        LambdaQueryWrapper<ScheduleTransferRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(ScheduleTransferRequest::getStatus, status);
        wrapper.orderByDesc(ScheduleTransferRequest::getCreatedAt);
        return transferMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
