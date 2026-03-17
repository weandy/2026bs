package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.result.Result;
import com.chp.medical.service.PrescriptionPdfService;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.entity.VaccineRecord;
import com.chp.resident.mapper.HealthRecordMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import com.chp.resident.mapper.VaccineRecordMapper;
import com.chp.security.util.SecurityUtils;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 居民健康摘要 PDF 导出（放在 chp-medical，有 iText 依赖）
 */
@Slf4j
@RestController
@RequestMapping("/resident/health-summary")
@RequiredArgsConstructor
public class HealthSummaryController {

    private final HealthRecordMapper healthRecordMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final VaccineRecordMapper vaccineRecordMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> summaryPdf() {
        // 安全：从 JWT Token 中获取当前登录居民的 ID，不接受外部传入
        Long residentId = SecurityUtils.getCurrentUserId();
        if (residentId == null) {
            return ResponseEntity.status(401).build();
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            var font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            doc.setFont(font);

            doc.add(new Paragraph("居民健康摘要").setFontSize(18).setBold());
            doc.add(new Paragraph("生成时间: " + java.time.LocalDate.now().format(FMT)).setFontSize(10));

            // 健康档案
            HealthRecord hr = healthRecordMapper.selectOne(
                    new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, residentId));
            if (hr != null) {
                doc.add(new Paragraph("一、基本信息").setFontSize(14).setBold());
                doc.add(new Paragraph("过敏史: " + (hr.getAllergyHistory() != null ? hr.getAllergyHistory() : "无")));
                doc.add(new Paragraph("慢性病标签: " + (hr.getChronicTags() != null ? hr.getChronicTags() : "无")));
                doc.add(new Paragraph("家族史: " + (hr.getFamilyHistory() != null ? hr.getFamilyHistory() : "无")));
            }

            // 近期就诊(5条)
            List<VisitRecord> visits = visitRecordMapper.selectList(
                    new LambdaQueryWrapper<VisitRecord>()
                            .eq(VisitRecord::getResidentId, residentId)
                            .orderByDesc(VisitRecord::getCreatedAt).last("LIMIT 5"));
            if (!visits.isEmpty()) {
                doc.add(new Paragraph("二、近期就诊记录").setFontSize(14).setBold());
                Table table = new Table(3);
                table.addCell(new Cell().add(new Paragraph("日期")));
                table.addCell(new Cell().add(new Paragraph("科室")));
                table.addCell(new Cell().add(new Paragraph("诊断")));
                for (VisitRecord v : visits) {
                    table.addCell(v.getVisitDate() != null ? v.getVisitDate().format(FMT) : "");
                    table.addCell(v.getDeptName() != null ? v.getDeptName() : "");
                    table.addCell(v.getDiagnosisNames() != null ? v.getDiagnosisNames() : "");
                }
                doc.add(table);
            }

            // 近期接种(5条)
            List<VaccineRecord> vaccines = vaccineRecordMapper.selectList(
                    new LambdaQueryWrapper<VaccineRecord>()
                            .eq(VaccineRecord::getResidentId, residentId)
                            .orderByDesc(VaccineRecord::getVaccinatedAt).last("LIMIT 5"));
            if (!vaccines.isEmpty()) {
                doc.add(new Paragraph("三、近期接种记录").setFontSize(14).setBold());
                Table vt = new Table(3);
                vt.addCell(new Cell().add(new Paragraph("日期")));
                vt.addCell(new Cell().add(new Paragraph("疫苗")));
                vt.addCell(new Cell().add(new Paragraph("剂次")));
                for (VaccineRecord vr : vaccines) {
                    vt.addCell(vr.getVaccinatedAt() != null ? vr.getVaccinatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
                    vt.addCell(vr.getVaccineName() != null ? vr.getVaccineName() : "");
                    vt.addCell(vr.getDoseNum() != null ? "第" + vr.getDoseNum() + "剂" : "");
                }
                doc.add(vt);
            }

            doc.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=health_summary.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("生成健康摘要PDF失败", e);
            throw new RuntimeException("生成 PDF 失败", e);
        }
    }
}
