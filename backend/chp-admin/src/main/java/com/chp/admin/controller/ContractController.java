package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.FamilyDoctorContract;
import com.chp.admin.mapper.FamilyDoctorContractMapper;
import com.chp.common.result.Result;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/contract")
@RequiredArgsConstructor
public class ContractController {

    private final FamilyDoctorContractMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    /** 签约列表（支持按状态筛选） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FamilyDoctorContract> qw = new LambdaQueryWrapper<FamilyDoctorContract>()
                .eq(status != null && !status.isBlank(), FamilyDoctorContract::getStatus, status)
                .orderByDesc(FamilyDoctorContract::getCreatedAt);
        // Bug6修复：keyword 同时搜索医生姓名和居民姓名
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(FamilyDoctorContract::getDoctorName, keyword)
                         .or().like(FamilyDoctorContract::getResidentName, keyword));
        }
        return Result.success(mapper.selectPage(new Page<>(page, size), qw));
    }

    /** 批准签约 */
    @PutMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id) {
        FamilyDoctorContract c = mapper.selectById(id);
        if (c == null) return Result.error(404, "签约记录不存在");
        if (!"PENDING".equals(c.getStatus())) return Result.error(400, "只有待审核的申请可以批准");

        c.setStatus("ACTIVE");
        c.setOperatorId(SecurityUtils.getCurrentUserId());
        c.setApproveTime(LocalDateTime.now());
        c.setStartDate(LocalDate.now());
        c.setEndDate(LocalDate.now().plusYears(1));
        mapper.updateById(c);
        return Result.success(c);
    }

    /** 驳回签约 */
    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FamilyDoctorContract c = mapper.selectById(id);
        if (c == null) return Result.error(404, "签约记录不存在");
        if (!"PENDING".equals(c.getStatus())) return Result.error(400, "只有待审核的申请可以驳回");

        c.setStatus("REJECTED");
        c.setRejectReason(body.getOrDefault("reason", "管理员驳回"));
        c.setOperatorId(SecurityUtils.getCurrentUserId());
        c.setApproveTime(LocalDateTime.now());
        mapper.updateById(c);
        return Result.success();
    }

    /** 管理员直接解约 */
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        FamilyDoctorContract c = mapper.selectById(id);
        if (c == null) return Result.error(404, "签约记录不存在");
        if (!"ACTIVE".equals(c.getStatus())) return Result.error(400, "只有生效中的签约可以解约");

        c.setStatus("CANCELLED");
        c.setCancelReason(body != null ? body.getOrDefault("reason", "管理员操作解约") : "管理员操作解约");
        c.setOperatorId(SecurityUtils.getCurrentUserId());
        mapper.updateById(c);
        return Result.success();
    }

    /** 各医生签约人数统计 */
    @GetMapping("/stats")
    public Result<?> stats() {
        List<Map<String, Object>> stats = jdbcTemplate.queryForList(
                "SELECT c.doctor_id AS doctorId, c.doctor_name AS doctorName, " +
                "COUNT(*) AS contractCount " +
                "FROM family_doctor_contract c WHERE c.status = 'ACTIVE' " +
                "GROUP BY c.doctor_id, c.doctor_name " +
                "ORDER BY contractCount DESC");
        return Result.success(stats);
    }

    /** 管理员手动创建签约 */
    @PostMapping
    public Result<?> create(@RequestBody FamilyDoctorContract contract) {
        contract.setStatus("ACTIVE");
        contract.setApplyTime(LocalDateTime.now());
        contract.setApproveTime(LocalDateTime.now());
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusYears(1));
        contract.setOperatorId(SecurityUtils.getCurrentUserId());
        mapper.insert(contract);
        return Result.success(contract);
    }

    /** 更新签约信息 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyDoctorContract contract) {
        contract.setId(id);
        mapper.updateById(contract);
        return Result.success();
    }
}
