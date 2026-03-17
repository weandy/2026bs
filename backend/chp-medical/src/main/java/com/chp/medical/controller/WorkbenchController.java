package com.chp.medical.controller;
import com.chp.common.result.Result;
import com.chp.resident.entity.VisitRecord;
import com.chp.medical.service.WorkbenchService;
import com.chp.medical.dto.CompleteVisitDTO;
import com.chp.resident.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.chp.common.annotation.AuditLog;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/queue")
    public Result<List<Appointment>> todayQueue() {
        return Result.success(workbenchService.todayQueue());
    }

    @PutMapping("/call/{appointmentId}")
    @AuditLog(type = "CALL_NEXT", module = "workbench", description = "叫号")
    public Result<Void> callNext(@PathVariable Long appointmentId) {
        workbenchService.callNext(appointmentId);
        return Result.success();
    }

    @PostMapping("/start/{appointmentId}")
    @AuditLog(type = "VISIT_START", module = "workbench", description = "开始接诊")
    public Result<VisitRecord> startVisit(@PathVariable Long appointmentId) {
        return Result.success(workbenchService.startVisit(appointmentId));
    }

    @PutMapping("/complete/{visitId}")
    @AuditLog(type = "VISIT_COMPLETE", module = "workbench", description = "完成接诊")
    public Result<Map<String, Object>> completeVisit(@PathVariable Long visitId, @RequestBody CompleteVisitDTO dto) {
        Map<String, Object> result = workbenchService.completeVisit(visitId, dto.getChiefComplaint(), dto.getDiagnosis(), dto.getTreatmentPlan());
        return Result.success(result);
    }
}
