package com.chp.medical.controller;

import com.chp.common.result.Result;
import com.chp.medical.dto.PrescriptionCreateDTO;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.medical.service.PrescriptionService;
import com.chp.medical.service.PrescriptionPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/medical/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionPdfService pdfService;

    @PostMapping("/{visitId}")
    public Result<Prescription> create(@PathVariable Long visitId, @Valid @RequestBody PrescriptionCreateDTO dto) {
        List<PrescriptionItem> items = dto.getItems().stream().map(i -> {
            PrescriptionItem item = new PrescriptionItem();
            item.setDrugId(i.getDrugId());
            item.setDrugName(i.getDrugName());
            item.setDrugSpec(i.getDrugSpec());
            item.setQuantity(i.getQuantity());
            item.setUsageMethod(i.getUsageMethod());
            item.setDosage(i.getDosage());
            item.setFrequency(i.getFrequency());
            item.setDays(i.getDays());
            return item;
        }).toList();
        return Result.success(prescriptionService.createPrescription(visitId, dto.getResidentId(), items));
    }

    @GetMapping("/visit/{visitId}")
    public Result<List<Prescription>> listByVisit(@PathVariable Long visitId) {
        return Result.success(prescriptionService.getByVisitId(visitId));
    }

    @GetMapping("/{prescriptionId}/items")
    public Result<List<PrescriptionItem>> items(@PathVariable Long prescriptionId) {
        return Result.success(prescriptionService.getItems(prescriptionId));
    }

    /** POST /api/medical/prescription/{id}/pdf — 下载处方 PDF */
    @PostMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prescription_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
