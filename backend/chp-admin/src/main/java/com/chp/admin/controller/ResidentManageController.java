package com.chp.admin.controller;

import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * A4: 居民管理
 */
@RestController
@RequestMapping("/admin/resident")
@RequiredArgsConstructor
public class ResidentManageController {

    private final JdbcTemplate jdbcTemplate;

    /** 居民列表（分页+搜索） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String keyword) {
        int offset = (page - 1) * size;
        String where = "WHERE 1=1";
        if (keyword != null && !keyword.isBlank()) {
            where += " AND (name LIKE '%" + keyword + "%' OR phone LIKE '%" + keyword + "%' OR id_card LIKE '%" + keyword + "%')";
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chp_resident.resident " + where, Long.class);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT id, id_card AS idCard, name, gender, birth_date AS birthDate, " +
                "phone, blood_type AS bloodType, address, status, created_at AS createdAt " +
                "FROM chp_resident.resident " + where +
                " ORDER BY id DESC LIMIT ? OFFSET ?", size, offset);
        return Result.success(Map.of("records", records, "total", total != null ? total : 0));
    }

    /** 居民详情 */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Map<String, Object> resident = jdbcTemplate.queryForMap(
                "SELECT id, id_card AS idCard, name, gender, birth_date AS birthDate, " +
                "phone, blood_type AS bloodType, address, status " +
                "FROM chp_resident.resident WHERE id = ?", id);
        return Result.success(resident);
    }

    /** 启/停居民账号 */
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        jdbcTemplate.update("UPDATE chp_resident.resident SET status = ? WHERE id = ?", status, id);
        return Result.success();
    }
}
