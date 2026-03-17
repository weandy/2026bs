package com.chp.medical.controller;

import com.chp.common.result.Result;
import com.chp.resident.entity.Appointment;
import com.chp.medical.service.WorkbenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 候诊公屏接口（公开，无需 Token）
 */
@RestController
@RequestMapping("/public/screen")
@RequiredArgsConstructor
public class PublicScreenController {

    private final WorkbenchService workbenchService;

    /**
     * 查询科室今日候诊/叫号列表
     * GET /api/public/screen/{deptCode}
     */
    @GetMapping("/{deptCode}")
    public Result<List<Appointment>> screen(@PathVariable String deptCode) {
        return Result.success(workbenchService.publicScreen(deptCode));
    }
}
