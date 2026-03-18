package com.chp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.VaccineStock;
import com.chp.admin.entity.VaccineStockLog;
import com.chp.admin.mapper.VaccineStockLogMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 疫苗库存管理服务
 * 注：vaccine = 品种主表，vaccine_stock = 批次明细表
 * 本 Service 用 JdbcTemplate 原生 SQL，通过 JOIN 聚合正确数据
 */
@Service
@RequiredArgsConstructor
public class VaccineStockService {

    private final JdbcTemplate jdbc;
    private final VaccineStockLogMapper vaccineStockLogMapper;

    // SQL：按疫苗品种聚合库存（SUM quantity，MIN expire_date）
    private static final String BASE_SQL = """
            SELECT v.id,
                   v.vaccine_code  AS vaccineCode,
                   v.vaccine_name  AS vaccineName,
                   v.applicable_age AS manufacturer,
                   COALESCE(SUM(CASE WHEN s.status=1 THEN s.quantity ELSE 0 END), 0) AS quantity,
                   MIN(CASE WHEN s.status=1 THEN s.batch_no END)   AS batchNo,
                   MIN(CASE WHEN s.status=1 THEN s.expire_date END) AS expiryDate,
                   1  AS status,
                   10 AS alertQty
            FROM chp_admin.vaccine v
            LEFT JOIN chp_admin.vaccine_stock s ON s.vaccine_id = v.id
            """;
    private static final String GROUP_SQL = "GROUP BY v.id, v.vaccine_code, v.vaccine_name, v.applicable_age ";

    /** 将 Map 行转换为 VaccineStock 对象 */
    private VaccineStock rowToStock(Map<String, Object> row) {
        VaccineStock vs = new VaccineStock();
        vs.setId(toLong(row.get("id")));
        vs.setVaccineCode(str(row.get("vaccineCode")));
        vs.setVaccineName(str(row.get("vaccineName")));
        vs.setManufacturer(str(row.get("manufacturer")));
        vs.setQuantity(toInt(row.get("quantity")));
        vs.setBatchNo(str(row.get("batchNo")));
        Object exp = row.get("expiryDate");
        vs.setExpiryDate(exp == null ? null : LocalDate.parse(exp.toString()));
        vs.setStatus(toInt(row.get("status")));
        vs.setAlertQty(toInt(row.get("alertQty")));
        return vs;
    }

    /** 疫苗库存列表（分页） */
    public Page<VaccineStock> list(int page, int size, String keyword) {
        String where = (keyword != null && !keyword.isBlank())
                ? "WHERE v.vaccine_name LIKE '%" + keyword.replace("'", "") + "%' "
                : "";
        String sql = BASE_SQL + where + GROUP_SQL + "ORDER BY v.id "
                + "LIMIT " + size + " OFFSET " + (long)(page - 1) * size;
        String countSql = "SELECT COUNT(*) FROM chp_admin.vaccine v " + where;

        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        long total = jdbc.queryForObject(countSql, Long.class);
        List<VaccineStock> records = rows.stream().map(this::rowToStock).toList();

        Page<VaccineStock> p = new Page<>(page, size, total);
        p.setRecords(records);
        return p;
    }

    /** 新增疫苗（插入 vaccine 主表） */
    public VaccineStock create(VaccineStock stock) {
        jdbc.update(
                "INSERT INTO chp_admin.vaccine(vaccine_code,vaccine_name,applicable_age) VALUES(?,?,?)",
                stock.getVaccineCode(), stock.getVaccineName(), stock.getManufacturer()
        );
        Long newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        stock.setId(newId);
        stock.setStatus(1);
        // 如果初始有库存，自动创建 vaccine_stock 批次
        if (stock.getQuantity() != null && stock.getQuantity() > 0) {
            String batchNo = stock.getBatchNo() != null ? stock.getBatchNo() : "INIT-" + newId;
            String expDate = stock.getExpiryDate() != null ? stock.getExpiryDate().toString() : LocalDate.now().plusYears(2).toString();
            jdbc.update(
                    "INSERT INTO chp_admin.vaccine_stock(vaccine_id,batch_no,quantity,expire_date,inbound_at,inbound_by,status) VALUES(?,?,?,?,?,?,1)",
                    newId, batchNo, stock.getQuantity(), expDate, LocalDate.now(), 1L
            );
        }
        return stock;
    }

    /** 更新疫苗（更新 vaccine 主表） */
    public void update(Long id, VaccineStock stock) {
        jdbc.update(
                "UPDATE chp_admin.vaccine SET vaccine_code=?, vaccine_name=?, applicable_age=? WHERE id=?",
                stock.getVaccineCode(), stock.getVaccineName(), stock.getManufacturer(), id
        );
    }

    /** 疫苗入库（插入 vaccine_stock 批次 + 写日志） */
    @Transactional
    public void addStock(Long vaccineId, int quantity, String batchNo) {
        // 查品种名
        String name = jdbc.queryForObject("SELECT vaccine_name FROM chp_admin.vaccine WHERE id=?", String.class, vaccineId);
        if (name == null) throw new BusinessException(StatusCode.NOT_FOUND, "疫苗不存在");
        String expDate = LocalDate.now().plusYears(2).toString();
        jdbc.update(
                "INSERT INTO chp_admin.vaccine_stock(vaccine_id,batch_no,quantity,expire_date,inbound_at,inbound_by,status) VALUES(?,?,?,?,?,?,1)",
                vaccineId, batchNo, quantity, expDate, LocalDate.now(), 1L
        );
        // 查当前总库存
        Integer balance = jdbc.queryForObject("SELECT COALESCE(SUM(quantity),0) FROM chp_admin.vaccine_stock WHERE vaccine_id=? AND status=1", Integer.class, vaccineId);
        // 写日志
        VaccineStockLog log = new VaccineStockLog();
        log.setVaccineId(vaccineId);
        log.setVaccineName(name);
        log.setBatchNo(batchNo);
        log.setOpType(1);
        log.setQuantity(quantity);
        log.setBalance(balance != null ? balance : quantity);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUser().getName());
        log.setOpTime(LocalDateTime.now());
        vaccineStockLogMapper.insert(log);
    }

    /** 疫苗出库 */
    @Transactional
    public void removeStock(Long vaccineId, int quantity, String reason) {
        String name = jdbc.queryForObject("SELECT vaccine_name FROM chp_admin.vaccine WHERE id=?", String.class, vaccineId);
        Integer cur = jdbc.queryForObject("SELECT COALESCE(SUM(quantity),0) FROM chp_admin.vaccine_stock WHERE vaccine_id=? AND status=1", Integer.class, vaccineId);
        if (cur == null || cur < quantity) throw new BusinessException(StatusCode.BAD_REQUEST, "库存不足");
        // 扣减最早批次
        jdbc.update("UPDATE chp_admin.vaccine_stock SET quantity = quantity - ? WHERE vaccine_id=? AND status=1 ORDER BY expire_date LIMIT 1", quantity, vaccineId);
        int balance = (cur - quantity);
        VaccineStockLog log = new VaccineStockLog();
        log.setVaccineId(vaccineId);
        log.setVaccineName(name);
        log.setOpType(2);
        log.setQuantity(quantity);
        log.setBalance(balance);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUser().getName());
        log.setOpTime(LocalDateTime.now());
        log.setRemark(reason);
        vaccineStockLogMapper.insert(log);
    }

    /** 效期预警（30天内过期的批次） */
    public List<VaccineStock> expiryAlert() {
        String sql = BASE_SQL + "WHERE s.status=1 AND s.expire_date <= ? " + GROUP_SQL + "ORDER BY expiryDate";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, LocalDate.now().plusDays(30).toString());
        return rows.stream().map(this::rowToStock).toList();
    }

    /** 库存不足预警（总量 <= alertQty=10） */
    public List<VaccineStock> stockAlert() {
        String sql = BASE_SQL + GROUP_SQL + "HAVING quantity <= 10 ORDER BY quantity";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        return rows.stream().map(this::rowToStock).toList();
    }

    /** 库存操作日志 */
    public Page<VaccineStockLog> logPage(int page, int size, Long vaccineId) {
        String where = vaccineId != null ? " WHERE vaccine_id=" + vaccineId : "";
        String sql = "SELECT * FROM chp_admin.vaccine_stock_log" + where + " ORDER BY op_time DESC LIMIT " + size + " OFFSET " + (long)(page-1)*size;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM chp_admin.vaccine_stock_log" + where, Long.class);
        List<VaccineStockLog> records = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            VaccineStockLog log = new VaccineStockLog();
            log.setId(toLong(r.get("id")));
            log.setVaccineId(toLong(r.get("vaccine_id")));
            log.setVaccineName(str(r.get("vaccine_name")));
            log.setBatchNo(str(r.get("batch_no")));
            log.setOpType(toInt(r.get("op_type")));
            log.setQuantity(toInt(r.get("quantity")));
            log.setBalance(toInt(r.get("balance")));
            log.setOperatorName(str(r.get("operator_name")));
            log.setRemark(str(r.get("remark")));
            Object opTime = r.get("op_time");
            if (opTime instanceof java.sql.Timestamp ts) log.setOpTime(ts.toLocalDateTime());
            records.add(log);
        }
        Page<VaccineStockLog> p = new Page<>(page, size, total);
        p.setRecords(records);
        return p;
    }

    // ── 工具方法 ──
    private static Long   toLong(Object o) { return o == null ? null : ((Number) o).longValue(); }
    private static int    toInt(Object o)  { return o == null ? 0 : ((Number) o).intValue(); }
    private static String str(Object o)    { return o == null ? null : o.toString(); }
}
