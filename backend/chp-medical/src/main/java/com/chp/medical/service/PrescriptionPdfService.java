package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.resident.mapper.PrescriptionMapper;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionPdfService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;

    public byte[] generatePdf(Long prescriptionId) {
        Prescription presc = prescriptionMapper.selectById(prescriptionId);
        if (presc == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "处方不存在");
        }
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            // 使用内建字体（Helvetica），中文环境可替换为中文字体
            PdfFont font = PdfFontFactory.createFont("Helvetica");

            // 标题
            doc.add(new Paragraph("Prescription / 处方单")
                    .setFont(font).setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            // 处方信息
            doc.add(new Paragraph("Presc No: " + presc.getPrescNo()
                    + "  |  Doctor: " + presc.getStaffName()
                    + "  |  Date: " + (presc.getCreatedAt() != null ?
                    presc.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""))
                    .setFont(font).setFontSize(10).setMarginBottom(8));

            // 药品明细表
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 1, 1, 2, 2, 1}))
                    .useAllAvailableWidth();

            String[] headers = {"#", "Drug Name", "Spec", "Qty", "Usage", "Dosage/Freq", "Days"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(font).setFontSize(9).setBold()));
            }

            int idx = 1;
            for (PrescriptionItem item : items) {
                table.addCell(cell(String.valueOf(idx++), font));
                table.addCell(cell(item.getDrugName() != null ? item.getDrugName() : "", font));
                table.addCell(cell(item.getDrugSpec() != null ? item.getDrugSpec() : "", font));
                table.addCell(cell(String.valueOf(item.getQuantity()), font));
                table.addCell(cell(item.getUsageMethod() != null ? item.getUsageMethod() : "", font));
                table.addCell(cell((item.getDosage() != null ? item.getDosage() : "") + " "
                        + (item.getFrequency() != null ? item.getFrequency() : ""), font));
                table.addCell(cell(item.getDays() != null ? String.valueOf(item.getDays()) : "", font));
            }

            doc.add(table);

            // 备注
            if (presc.getNotes() != null && !presc.getNotes().isBlank()) {
                doc.add(new Paragraph("Notes: " + presc.getNotes())
                        .setFont(font).setFontSize(9).setMarginTop(10));
            }

            // 底部
            doc.add(new Paragraph("Community Health Service Center")
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            doc.close();
            return baos.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private Cell cell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(8));
    }
}
