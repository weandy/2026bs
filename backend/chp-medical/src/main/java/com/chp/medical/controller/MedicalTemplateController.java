package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.MedicalTemplate;
import com.chp.admin.mapper.MedicalTemplateMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical/template")
@RequiredArgsConstructor
public class MedicalTemplateController {

    private final MedicalTemplateMapper mapper;

    /** 模板列表，支持按分类筛选 */
    @GetMapping
    public Result<List<MedicalTemplate>> list(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<MedicalTemplate> qw = new LambdaQueryWrapper<MedicalTemplate>()
                .eq(category != null, MedicalTemplate::getCategory, category)
                .eq(MedicalTemplate::getIsPublic, 1)
                .orderByDesc(MedicalTemplate::getCreatedAt);
        return Result.success(mapper.selectList(qw));
    }

    /** 创建自定义模板 */
    @PostMapping
    public Result<?> create(@RequestBody MedicalTemplate tpl,
                            @RequestAttribute("staffId") Long staffId) {
        tpl.setCreatorId(staffId);
        mapper.insert(tpl);
        return Result.success(tpl);
    }
}
