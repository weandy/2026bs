package com.chp.resident.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.result.Result;
import com.chp.resident.entity.Message;
import com.chp.resident.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/resident/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<Page<Message>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(messageService.myMessages(page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", messageService.unreadCount()));
    }
}
