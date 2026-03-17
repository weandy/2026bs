package com.chp.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Drug;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.mapper.DrugMapper;
import com.chp.admin.mapper.DrugStockMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 药品/库存管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugMapper drugMapper;
    private final DrugStockMapper drugStockMapper;
    private final JdbcTemplate jdbcTemplate;

    public Page<Drug> drugList(int page, int size, String keyword) {
        LambdaQueryWrapper<Drug> q = new LambdaQueryWrapper<Drug>().orderByAsc(Drug::getDrugCode);
        if (keyword != null && !keyword.isBlank()) {
            q.like(Drug::getGenericName, keyword);
        }
        return drugMapper.selectPage(new Page<>(page, size), q);
    }

    public Drug createDrug(Drug drug) { drugMapper.insert(drug); return drug; }
    public void updateDrug(Drug drug) { drugMapper.updateById(drug); }

    public List<DrugStock> stockByDrug(Long drugId) {
        return drugStockMapper.selectList(
                new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, drugId));
    }

    @Transactional
    public DrugStock addStock(DrugStock stock) {
        drugStockMapper.insert(stock);
        return stock;
    }

    /** 出入库日志（分页，支持关键字/类型过滤） */
    public java.util.Map<String, Object> stockLogs(int page, int size, String keyword, Integer changeType) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT dsl.*, d.generic_name AS drug_name FROM drug_stock_log dsl " +
                "LEFT JOIN drug_stock ds ON ds.id = dsl.stock_id " +
                "LEFT JOIN drug d ON d.id = ds.drug_id WHERE 1=1");
            java.util.List<Object> params = new java.util.ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                sql.append(" AND d.generic_name LIKE ?");
                params.add("%%" + keyword + "%%");
            }
            if (changeType != null) {
                sql.append(" AND dsl.change_type = ?");
                params.add(changeType);
            }
            // 总数
            String countSql = "SELECT COUNT(*) FROM (" + sql + ") t";
            Long total = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);
            // 分页
            sql.append(" ORDER BY dsl.created_at DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);
            var records = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            result.put("records", records);
            result.put("total", total != null ? total : 0);
        } catch (Exception e) {
            log.error("查询出入库日志失败", e);
            result.put("records", java.util.Collections.emptyList());
            result.put("total", 0);
        }
        return result;
    }

    /** 效期看板（全部库存，包含已过期和将过期） */
    public java.util.List<java.util.Map<String, Object>> expiryBoard() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT ds.*, d.generic_name, d.supplier FROM drug_stock ds " +
                "LEFT JOIN drug d ON d.id = ds.drug_id " +
                "WHERE ds.expire_date IS NOT NULL " +
                "ORDER BY ds.expire_date ASC");
        } catch (Exception e) {
            log.error("查询效期看板失败", e);
            return java.util.Collections.emptyList();
        }
    }
}
