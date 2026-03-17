package com.chp.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.VisitAttachment;
import com.chp.admin.mapper.VisitAttachmentMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medical/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final VisitAttachmentMapper mapper;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam Long visitId,
                            @RequestParam MultipartFile file,
                            @RequestAttribute("staffId") Long staffId) throws IOException {
        String dir = UPLOAD_DIR + visitId + "/";
        new File(dir).mkdirs();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(new File(dir + fileName));

        VisitAttachment att = new VisitAttachment();
        att.setVisitId(visitId);
        att.setFileName(file.getOriginalFilename());
        att.setFileUrl("/uploads/" + visitId + "/" + fileName);
        att.setFileType(detectType(file.getOriginalFilename()));
        att.setFileSize(file.getSize());
        att.setUploadedBy(staffId);
        mapper.insert(att);
        return Result.success(att);
    }

    @GetMapping("/{visitId}")
    public Result<List<VisitAttachment>> list(@PathVariable Long visitId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<VisitAttachment>()
                        .eq(VisitAttachment::getVisitId, visitId)
                        .orderByDesc(VisitAttachment::getCreatedAt)));
    }

    private String detectType(String name) {
        if (name == null) return "other";
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg"))
            return "image";
        return "report";
    }
}
