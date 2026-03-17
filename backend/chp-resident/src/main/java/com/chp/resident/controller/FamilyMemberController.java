package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.resident.entity.FamilyMember;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.entity.HealthVital;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.mapper.FamilyMemberMapper;
import com.chp.resident.mapper.HealthRecordMapper;
import com.chp.resident.mapper.HealthVitalMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resident/family")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberMapper mapper;
    private final HealthRecordMapper healthRecordMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final HealthVitalMapper healthVitalMapper;

    @GetMapping
    public Result<List<FamilyMember>> list(@RequestAttribute("residentId") Long residentId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getOwnerId, residentId)
                        .eq(FamilyMember::getStatus, 1)));
    }

    @PostMapping
    public Result<?> add(@RequestBody FamilyMember member,
                         @RequestAttribute("residentId") Long residentId) {
        member.setOwnerId(residentId);
        member.setStatus(1);
        // 自动设置权限范围（创新点③）
        if (member.getPermissionScope() == null || member.getPermissionScope().isBlank()) {
            member.setPermissionScope(defaultScope(member.getRelation()));
        }
        mapper.insert(member);
        return Result.success(member);
    }

    /** 代查看接口（创新点③）—— 根据 permissionScope 过滤返回被代管人的健康数据 */
    @GetMapping("/{id}/proxy-view")
    public Result<?> proxyView(@PathVariable Long id,
                                @RequestAttribute("residentId") Long residentId) {
        FamilyMember fm = mapper.selectById(id);
        if (fm == null || !fm.getOwnerId().equals(residentId) || fm.getStatus() != 1)
            return Result.error(403, "无权查看");
        if (fm.getLinkedResidentId() == null)
            return Result.error(400, "该家庭成员尚未关联居民账号");

        Set<String> scopes = Arrays.stream(
                (fm.getPermissionScope() != null ? fm.getPermissionScope() : "basic").split(","))
                .map(String::trim).collect(Collectors.toSet());

        Long targetId = fm.getLinkedResidentId();
        Map<String, Object> data = new HashMap<>();
        data.put("memberName", fm.getMemberName());
        data.put("relation", fm.getRelation());
        data.put("scopes", scopes);

        if (scopes.contains("health_record")) {
            HealthRecord hr = healthRecordMapper.selectOne(
                    new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, targetId));
            if (hr != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("chronicTags", hr.getChronicTags());
                record.put("allergyHistory", hr.getAllergyHistory());
                record.put("familyHistory", hr.getFamilyHistory());
                data.put("healthRecord", record);
            }
        }

        if (scopes.contains("vital")) {
            List<HealthVital> vitals = healthVitalMapper.selectList(
                    new LambdaQueryWrapper<HealthVital>()
                            .eq(HealthVital::getResidentId, targetId)
                            .orderByDesc(HealthVital::getMeasureTime)
                            .last("LIMIT 10"));
            data.put("recentVitals", vitals);
        }

        if (scopes.contains("visit_record")) {
            List<VisitRecord> visits = visitRecordMapper.selectList(
                    new LambdaQueryWrapper<VisitRecord>()
                            .eq(VisitRecord::getResidentId, targetId)
                            .orderByDesc(VisitRecord::getCreatedAt)
                            .last("LIMIT 5"));
            data.put("recentVisits", visits);
        }

        return Result.success(data);
    }

    /** 根据关系自动赋予默认权限 */
    private String defaultScope(String relation) {
        if (relation == null) return "basic,appointment";
        switch (relation) {
            case "父亲": case "母亲": case "父": case "母":
                return "basic,appointment,health_record,vital";
            case "配偶": case "丈夫": case "妻子":
                return "basic,appointment,health_record,visit_record,vital";
            case "子女": case "儿子": case "女儿":
                return "basic,appointment,health_record,visit_record,vital";
            default:
                return "basic,appointment";
        }
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyMember member,
                            @RequestAttribute("residentId") Long residentId) {
        FamilyMember existing = mapper.selectById(id);
        if (existing == null || !existing.getOwnerId().equals(residentId))
            return Result.error(400, "无权操作");
        member.setId(id);
        member.setOwnerId(residentId);
        mapper.updateById(member);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestAttribute("residentId") Long residentId) {
        FamilyMember m = mapper.selectById(id);
        if (m == null || !m.getOwnerId().equals(residentId))
            return Result.error(400, "无权操作");
        m.setStatus(0);
        mapper.updateById(m);
        return Result.success();
    }
}
