package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.ConsultationRequest;
import com.chp.admin.mapper.ConsultationRequestMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/medical/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationRequestMapper mapper;

    @PostMapping
    public Result<?> create(@RequestBody ConsultationRequest req,
                            @RequestAttribute("staffId") Long staffId) {
        req.setRequesterId(staffId);
        req.setStatus(1);
        mapper.insert(req);
        return Result.success(req);
    }

    @GetMapping
    public Result<List<ConsultationRequest>> list(@RequestAttribute("staffId") Long staffId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<ConsultationRequest>()
                        .eq(ConsultationRequest::getRequesterId, staffId)
                        .orderByDesc(ConsultationRequest::getCreatedAt)));
    }

    @PutMapping("/{id}/respond")
    public Result<?> respond(@PathVariable Long id, @RequestBody ConsultationRequest body,
                             @RequestAttribute("staffId") Long staffId) {
        ConsultationRequest req = mapper.selectById(id);
        if (req == null) return Result.error(400, "会诊请求不存在");
        req.setStatus(body.getStatus());
        req.setResponseNote(body.getResponseNote());
        req.setResponderId(staffId);
        req.setRespondedAt(LocalDateTime.now());
        mapper.updateById(req);
        return Result.success();
    }
}
