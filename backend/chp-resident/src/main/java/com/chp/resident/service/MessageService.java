package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.resident.entity.Message;
import com.chp.resident.mapper.MessageMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public Page<Message> myMessages(int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getResidentId, userId)
                        .orderByDesc(Message::getCreatedAt));
    }

    public void markAsRead(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, id)
                .eq(Message::getResidentId, userId)
                .set(Message::getIsRead, 1));
    }

    public long unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getResidentId, userId)
                .eq(Message::getIsRead, 0));
    }
}
