package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.HealthVital;
import com.chp.resident.mapper.HealthVitalMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医护端健康指标录入接口（创新点①）
 */
@RestController
@RequestMapping("/medical/vital")
@RequiredArgsConstructor
public class MedicalVitalController {

    private final HealthVitalMapper vitalMapper;

    /**
     * 为指定居民录入一条健康指标（血压、血糖、体重）
     */
    @PostMapping("/{residentId}")
    public Result<?> record(@PathVariable Long residentId, @RequestBody HealthVital vital) {
        vital.setResidentId(residentId);
        vital.setRecordedBy(SecurityUtils.getCurrentUserId());
        vital.setRecordedByName(SecurityUtils.getCurrentUser().getName());
        if (vital.getMeasureTime() == null) vital.setMeasureTime(LocalDateTime.now());

        vitalMapper.insert(vital);

        // 异常检测：检查最近 N 条同类型指标是否连续超标
        Map<String, Object> anomaly = checkAnomaly(residentId, vital.getVitalType());
        Map<String, Object> result = new HashMap<>();
        result.put("vital", vital);
        result.put("anomaly", anomaly);

        return Result.success(result);
    }

    /**
     * 查询某居民的指标历史（医护端用，不受 residentId 限制）
     */
    @GetMapping("/{residentId}")
    public Result<List<HealthVital>> list(@PathVariable Long residentId,
                                          @RequestParam String type,
                                          @RequestParam(defaultValue = "90") Integer days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<HealthVital> list = vitalMapper.selectList(
                new LambdaQueryWrapper<HealthVital>()
                        .eq(HealthVital::getResidentId, residentId)
                        .eq(HealthVital::getVitalType, type)
                        .ge(HealthVital::getMeasureTime, since)
                        .orderByDesc(HealthVital::getMeasureTime));
        return Result.success(list);
    }

    /**
     * 异常检测：取最近 3 条记录，若全部超标则返回异常信息
     */
    private Map<String, Object> checkAnomaly(Long residentId, String vitalType) {
        Map<String, Object> result = new HashMap<>();
        result.put("abnormal", false);

        List<HealthVital> recent = vitalMapper.selectList(
                new LambdaQueryWrapper<HealthVital>()
                        .eq(HealthVital::getResidentId, residentId)
                        .eq(HealthVital::getVitalType, vitalType)
                        .orderByDesc(HealthVital::getMeasureTime)
                        .last("LIMIT 3"));

        if (recent.size() < 3) return result;

        boolean allAbnormal = recent.stream().allMatch(v -> isAbnormal(v.getVitalType(), v.getVitalValue()));
        if (allAbnormal) {
            result.put("abnormal", true);
            result.put("message", "该居民 " + vitalType + " 指标连续 3 次超标，请重点关注！");
            result.put("consecutiveCount", 3);
        }

        return result;
    }

    private boolean isAbnormal(String type, String value) {
        try {
            switch (type) {
                case "blood_pressure": {
                    // 格式：120/80
                    String[] parts = value.split("/");
                    int sys = Integer.parseInt(parts[0].trim());
                    int dia = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                    return sys > 140 || dia > 90;
                }
                case "blood_glucose": {
                    double glucose = Double.parseDouble(value.trim());
                    return glucose > 7.0 || glucose < 3.9;
                }
                case "weight": {
                    // 体重异常不做简单阈值检测
                    return false;
                }
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
