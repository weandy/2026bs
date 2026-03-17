package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chp.admin.entity.ScheduleTransferRequest;
import com.chp.admin.service.ScheduleTransferService;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/schedule/transfer")
@RequiredArgsConstructor
public class ScheduleTransferController {

    private final ScheduleTransferService transferService;

    /** POST /api/admin/schedule/transfer — 提交调班申请 */
    @PostMapping
    public Result<Void> submit(@RequestBody ScheduleTransferRequest req) {
        transferService.submit(req);
        return Result.success();
    }

    /** PUT /api/admin/schedule/transfer/{id} — 审批 */
    @PutMapping("/{id}")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String comment = (String) body.get("comment");
        transferService.review(id, status, comment);
        return Result.success();
    }

    /** GET /api/admin/schedule/transfer?page=1&size=10&status=0 */
    @GetMapping
    public Result<IPage<ScheduleTransferRequest>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(transferService.list(page, size, status));
    }
}
