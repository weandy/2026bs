package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.HealthVital;
import com.chp.resident.mapper.HealthVitalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resident/vital")
@RequiredArgsConstructor
public class HealthVitalController {

    private final HealthVitalMapper mapper;

    /** 录入一条健康指标 */
    @PostMapping
    public Result<?> record(@RequestBody HealthVital vital,
                            @RequestAttribute("residentId") Long residentId) {
        vital.setResidentId(residentId);
        if (vital.getMeasureTime() == null) vital.setMeasureTime(LocalDateTime.now());
        mapper.insert(vital);
        return Result.success(vital);
    }

    /** 按类型查询历史数据 */
    @GetMapping
    public Result<List<HealthVital>> list(@RequestParam String type,
                                         @RequestParam(defaultValue = "30") Integer days,
                                         @RequestAttribute("residentId") Long residentId) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<HealthVital> list = mapper.selectList(
                new LambdaQueryWrapper<HealthVital>()
                        .eq(HealthVital::getResidentId, residentId)
                        .eq(HealthVital::getVitalType, type)
                        .ge(HealthVital::getMeasureTime, since)
                        .orderByDesc(HealthVital::getMeasureTime));
        return Result.success(list);
    }

    /** 获取最新一条各指标 */
    @GetMapping("/latest")
    public Result<Map<String, HealthVital>> latest(@RequestAttribute("residentId") Long residentId) {
        List<HealthVital> all = mapper.selectList(
                new LambdaQueryWrapper<HealthVital>()
                        .eq(HealthVital::getResidentId, residentId)
                        .orderByDesc(HealthVital::getMeasureTime));
        Map<String, HealthVital> latest = all.stream()
                .collect(Collectors.toMap(HealthVital::getVitalType, v -> v, (a, b) -> a));
        return Result.success(latest);
    }
}
