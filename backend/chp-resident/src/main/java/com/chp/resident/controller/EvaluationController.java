package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.VisitEvaluation;
import com.chp.resident.mapper.VisitEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resident/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final VisitEvaluationMapper mapper;

    @PostMapping
    public Result<?> submit(@RequestBody VisitEvaluation evaluation,
                            @RequestAttribute("residentId") Long residentId) {
        evaluation.setResidentId(residentId);
        // 检查是否已评价
        Long exists = mapper.selectCount(
                new LambdaQueryWrapper<VisitEvaluation>()
                        .eq(VisitEvaluation::getVisitId, evaluation.getVisitId()));
        if (exists > 0) return Result.error(400, "该就诊已评价");
        mapper.insert(evaluation);
        return Result.success(evaluation);
    }

    @GetMapping("/{visitId}")
    public Result<VisitEvaluation> get(@PathVariable Long visitId) {
        VisitEvaluation eval = mapper.selectOne(
                new LambdaQueryWrapper<VisitEvaluation>()
                        .eq(VisitEvaluation::getVisitId, visitId));
        return Result.success(eval);
    }
}
