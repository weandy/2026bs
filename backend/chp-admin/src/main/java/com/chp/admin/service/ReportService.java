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
     * 近7天排班量趋势（基于排班表统计，反映门诊开放量）
     */
    public Map<String, Object> visitTrend7Days() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.toString());
            // 真实查询：按天统计排班数（代表就诊量趋势）
            long count = scheduleMapper.selectCount(
                    new LambdaQueryWrapper<Schedule>()
                            .eq(Schedule::getScheduleDate, day)
                            .eq(Schedule::getIsStopped, 0));
            counts.add((int) count);
        }
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    /**
     * 科室预约分布 — 基于排班表统计各科室排班数量
     */
    public Map<String, Object> deptLoadDistribution() {
        Map<String, Object> result = new HashMap<>();
        // 真实查询：按科室分组统计本月排班数
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Schedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .ge(Schedule::getScheduleDate, firstDayOfMonth)
                        .eq(Schedule::getIsStopped, 0));

        Map<String, Long> deptCounts = schedules.stream()
                .collect(Collectors.groupingBy(Schedule::getDeptName, Collectors.counting()));

        List<Map<String, Object>> deptData = deptCounts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        result.put("deptData", deptData);
        return result;
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

    /** 签约统计 */
    public Map<String, Object> contractReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM service_contract", Integer.class);
            Integer active = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM service_contract WHERE status = 1", Integer.class);
            Integer expired = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM service_contract WHERE status != 1", Integer.class);
            Integer thisMonth = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM service_contract WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())",
                    Integer.class);

            result.put("total", total != null ? total : 0);
            result.put("active", active != null ? active : 0);
            result.put("expired", expired != null ? expired : 0);
            result.put("thisMonth", thisMonth != null ? thisMonth : 0);
        } catch (Exception e) {
            log.warn("查询签约统计失败", e);
        }
        return result;
    }
}
