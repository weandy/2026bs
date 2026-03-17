package com.chp.admin.controller;

import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ICD-10 疾病编码检索
 */
@RestController
@RequestMapping("/admin/icd")
@RequiredArgsConstructor
public class IcdController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 模糊搜索 ICD-10 编码
     * GET /api/admin/icd/search?keyword=高血压
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() < 1) {
            return Result.success(Collections.emptyList());
        }
        String like = "%" + keyword + "%";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT code, name_cn AS nameCn, category, pinyin_code AS pinyinCode " +
                "FROM icd_code WHERE code LIKE ? OR name_cn LIKE ? OR pinyin_code LIKE ? " +
                "ORDER BY code LIMIT 20",
                like, like, like);
        return Result.success(results);
    }
}
