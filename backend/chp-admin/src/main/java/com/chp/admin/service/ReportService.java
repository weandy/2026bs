package com.chp.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.Drug;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.entity.DrugStockLog;
import com.chp.admin.mapper.DrugMapper;
import com.chp.admin.mapper.DrugStockLogMapper;
import com.chp.admin.mapper.DrugStockMapper;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.PublicHealthRecord;
import com.chp.admin.mapper.PublicHealthRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DrugStockLogMapper drugStockLogMapper;
    private final DrugMapper drugMapper;
    private final DrugStockMapper drugStockMapper;
    private final ScheduleMapper scheduleMapper;
    private final PublicHealthRecordMapper publicHealthRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 统计概览：今日挂号/就诊/在线医生/药品预警
     * 注意：跨库数据需分别查询，此处仅返回admin库内的数据
     */
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        // 药品预警统计
        List<Drug> drugs = drugMapper.selectList(
                new LambdaQueryWrapper<Drug>().eq(Drug::getStatus, 1));
        int alertCount = 0;
        for (Drug drug : drugs) {
            List<DrugStock> stocks = drugStockMapper.selectList(
                    new LambdaQueryWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, drug.getId())
                            .eq(DrugStock::getStatus, 1));
            int totalQty = stocks.stream().mapToInt(DrugStock::getQuantity).sum();
            if (totalQty < drug.getAlertQty()) {
                alertCount++;
            }
        }
        result.put("drugAlertCount", alertCount);
        result.put("totalDrugs", drugs.size());
        return result;
    }

    /**
     * 药品消耗统计
     */
    public Map<String, Object> drugReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // 查询出库记录 (direction=2 出库)
        List<DrugStockLog> logs = drugStockLogMapper.selectList(
                new LambdaQueryWrapper<DrugStockLog>()
                        .eq(DrugStockLog::getDirection, 2)
                        .ge(DrugStockLog::getOpTime, start)
                        .lt(DrugStockLog::getOpTime, end));

        // 按药品分组统计消耗总量
        Map<String, Integer> consumption = logs.stream()
                .collect(Collectors.groupingBy(
                        DrugStockLog::getDrugName,
                        Collectors.summingInt(DrugStockLog::getQuantity)));

        // TOP10
        List<Map<String, Object>> topConsumed = consumption.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("drugName", e.getKey());
                    m.put("quantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        result.put("totalOutbound", logs.stream().mapToInt(DrugStockLog::getQuantity).sum());
        result.put("topConsumed", topConsumed);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return result;
    }

    /**
     * 就诊趋势（按日期范围，从真实 visit_record 统计）
     * 默认近7天；前端可传 startDate/endDate 自定义范围
     */
    public Map<String, Object> visitTrend(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d.toString());
            try {
                Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM chp_resident.visit_record " +
                    "WHERE DATE(visit_date) = ? AND is_deleted = 0",
                    Integer.class, d);
                counts.add(cnt != null ? cnt : 0);
            } catch (Exception e) {
                counts.add(0);
            }
        }
        result.put("dates", dates);
        result.put("counts", counts);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        return result;
    }

    /** 兼容旧调用：默认近7天 */
    public Map<String, Object> visitTrend7Days() {
        LocalDate today = LocalDate.now();
        return visitTrend(today.minusDays(6), today);
    }

    /**
     * 科室就诊分布 — 从真实 visit_record 统计指定日期范围内各科室就诊量
     */
    public Map<String, Object> deptLoadDistribution(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT dept_name AS name, COUNT(*) AS value " +
                "FROM chp_resident.visit_record " +
                "WHERE DATE(visit_date) BETWEEN ? AND ? AND is_deleted = 0 " +
                "GROUP BY dept_name ORDER BY value DESC",
                startDate, endDate);
            result.put("deptData", rows);
        } catch (Exception e) {
            log.warn("查询科室分布失败", e);
            result.put("deptData", Collections.emptyList());
        }
        return result;
    }

    /** 兼容旧调用：默认近7天 */
    public Map<String, Object> deptLoadDistribution() {
        LocalDate today = LocalDate.now();
        return deptLoadDistribution(today.minusDays(6), today);
    }

    /**
     * 公卫服务统计 — 按服务类型分布
     */
    public Map<String, Object> publicHealthReport() {
        Map<String, Object> result = new HashMap<>();
        List<PublicHealthRecord> records = publicHealthRecordMapper.selectList(
                new LambdaQueryWrapper<PublicHealthRecord>()
                        .ge(PublicHealthRecord::getServiceDate, LocalDate.now().minusMonths(3)));

        Map<String, Long> typeCount = records.stream()
                .collect(Collectors.groupingBy(PublicHealthRecord::getServiceType, Collectors.counting()));

        List<Map<String, Object>> data = typeCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        result.put("publicHealthData", data);
        result.put("total", records.size());
        return result;
    }

    /** 接种统计 (用 JdbcTemplate 绕开跨模块依赖) */
    public Map<String, Object> vaccineReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 按疫苗分组统计
            List<Map<String, Object>> byVaccine = jdbcTemplate.queryForList(
                    "SELECT vaccine_name, COUNT(*) AS cnt FROM vaccine_record GROUP BY vaccine_name ORDER BY cnt DESC");
            result.put("byVaccine", byVaccine);

            // 按月统计
            List<Map<String, Object>> byMonth = jdbcTemplate.queryForList(
                    "SELECT DATE_FORMAT(vaccination_date, '%Y-%m') AS month, COUNT(*) AS cnt " +
                    "FROM vaccine_record GROUP BY month ORDER BY month DESC LIMIT 12");
            result.put("byMonth", byMonth);

            // 总数
            Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vaccine_record", Long.class);
            result.put("total", total != null ? total : 0);
        } catch (Exception e) {
            log.error("接种统计查询失败", e);
            result.put("total", 0);
            result.put("byVaccine", Collections.emptyList());
            result.put("byMonth", Collections.emptyList());
        }
        return result;
    }

    /**
     * 按日期/科室统计预约趋势
     */
    public Map<String, Object> appointmentTrend(LocalDate startDate, LocalDate endDate, String deptCode) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d.toString());
            LambdaQueryWrapper<Schedule> q = new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getScheduleDate, d)
                    .eq(Schedule::getIsStopped, 0);
            if (deptCode != null && !deptCode.isBlank()) {
                q.eq(Schedule::getDeptCode, deptCode);
            }
            counts.add(Math.toIntExact(scheduleMapper.selectCount(q)));
        }

        result.put("dates", dates);
        result.put("counts", counts);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return result;
    }

    /**
     * 导出报表概览（返回 EasyExcel 字节流）
     */
    public byte[] exportOverview() {
        Map<String, Object> overview = overview();
        Map<String, Object> trend = visitTrend7Days();

        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.alibaba.excel.EasyExcel.write(baos)
                    .sheet("统计概览")
                    .doWrite(() -> {
                        java.util.List<java.util.List<String>> data = new java.util.ArrayList<>();
                        // Header
                        java.util.List<String> header = java.util.List.of("指标", "值");
                        data.add(header);
                        data.add(java.util.List.of("药品预警数", String.valueOf(overview.getOrDefault("drugAlertCount", 0))));
                        data.add(java.util.List.of("药品总数", String.valueOf(overview.getOrDefault("totalDrugs", 0))));
                        // 趋势数据
                        @SuppressWarnings("unchecked")
                        java.util.List<String> trendDates = (java.util.List<String>) trend.get("dates");
                        @SuppressWarnings("unchecked")
                        java.util.List<Integer> trendCounts = (java.util.List<Integer>) trend.get("counts");
                        if (trendDates != null) {
                            for (int i = 0; i < trendDates.size(); i++) {
                                data.add(java.util.List.of(trendDates.get(i) + " 排班数",
                                        String.valueOf(trendCounts.get(i))));
                            }
                        }
                        return data;
                    });
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("导出报表失败", e);
            throw new RuntimeException("导出报表失败", e);
        }
    }

    /** 随访完成率统计 */
    public Map<String, Object> followUpReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer totalPlans = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM follow_up_plan WHERE is_deleted = 0", Integer.class);
            Integer completedPlans = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM follow_up_plan WHERE is_deleted = 0 AND status = 2", Integer.class);
            Integer activePlans = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM follow_up_plan WHERE is_deleted = 0 AND status = 1", Integer.class);
            Integer thisMonthDue = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM follow_up_plan WHERE is_deleted = 0 AND status = 1 AND next_follow_date <= LAST_DAY(CURDATE())",
                    Integer.class);
            Integer totalRecords = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM follow_up_record", Integer.class);

            result.put("totalPlans", totalPlans != null ? totalPlans : 0);
            result.put("completedPlans", completedPlans != null ? completedPlans : 0);
            result.put("activePlans", activePlans != null ? activePlans : 0);
            result.put("thisMonthDue", thisMonthDue != null ? thisMonthDue : 0);
            result.put("totalRecords", totalRecords != null ? totalRecords : 0);
            result.put("completionRate", totalPlans != null && totalPlans > 0
                    ? Math.round((completedPlans != null ? completedPlans : 0) * 100.0 / totalPlans) : 0);
        } catch (Exception e) {
            log.warn("查询随访统计失败", e);
        }
        return result;
    }

    /** 签约统计（使用 family_doctor_contract 表）*/
    public Map<String, Object> contractReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 累计有效签约（status=2 已签约生效）
            Integer activeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM family_doctor_contract WHERE status = 2", Integer.class);
            // 本月新增
            Integer monthNew = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM family_doctor_contract " +
                    "WHERE YEAR(sign_date) = YEAR(CURDATE()) AND MONTH(sign_date) = MONTH(CURDATE())",
                    Integer.class);
            // 待审核
            Integer pendingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM family_doctor_contract WHERE status = 1", Integer.class);
            // 总计
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM family_doctor_contract", Integer.class);

            result.put("activeCount",  activeCount  != null ? activeCount  : 0);
            result.put("monthNew",     monthNew     != null ? monthNew     : 0);
            result.put("pendingCount", pendingCount != null ? pendingCount : 0);
            result.put("total",        total        != null ? total        : 0);
        } catch (Exception e) {
            log.warn("查询签约统计失败", e);
        }
        return result;
    }

    /** 指定日期接诊概况（从真实 visit_record 统计）*/
    public Map<String, Object> dailySummary(LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 当日就诊记录总数（已完成）
            Integer completed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM chp_resident.visit_record " +
                    "WHERE DATE(visit_date) = ? AND is_deleted = 0",
                    Integer.class, date);
            // 当日预约总数（appointment 表）
            Integer totalAppt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM appointment WHERE appt_date = ? AND is_deleted = 0",
                    Integer.class, date);
            // 候诊中（status=1 待就诊）
            Integer waiting = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM appointment WHERE appt_date = ? AND status = 1 AND is_deleted = 0",
                    Integer.class, date);
            // 取消/爽约（status=5/6）
            Integer cancelled = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM appointment WHERE appt_date = ? AND status IN (5,6) AND is_deleted = 0",
                    Integer.class, date);

            result.put("date",      date.toString());
            result.put("totalAppt", totalAppt  != null ? totalAppt  : 0);
            result.put("completed", completed  != null ? completed  : 0);
            result.put("waiting",   waiting    != null ? waiting    : 0);
            result.put("cancelled", cancelled  != null ? cancelled  : 0);
            result.put("estimated", false);
        } catch (Exception e) {
            log.warn("查询日接诊概况失败: date={}", date, e);
            result.put("date",      date.toString());
            result.put("totalAppt", 0);
            result.put("completed", 0);
            result.put("waiting",   0);
            result.put("cancelled", 0);
            result.put("estimated", true);
        }
        return result;
    }

    /** 医生本周工作量排行 */
    public List<Map<String, Object>> doctorWorkloadRank() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT vr.staff_name AS name, COUNT(*) AS count " +
                "FROM visit_record vr " +
                "WHERE vr.visit_date >= CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY " +
                "GROUP BY vr.staff_id, vr.staff_name " +
                "ORDER BY count DESC LIMIT 10");
        } catch (Exception e) {
            log.warn("查询医生工作量失败", e);
            return Collections.emptyList();
        }
    }
}
