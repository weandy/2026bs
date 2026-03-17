package com.chp.resident.service;

import com.chp.resident.mapper.MessageMapper;
import com.chp.security.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 单元测试")
class MessageServiceTest {

    @Mock private MessageMapper messageMapper;

    @InjectMocks private MessageService messageService;

    @Test
    @DisplayName("未读数量——selectCount 返回正确值")
    void unreadCount_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(5L);
            when(messageMapper.selectCount(any())).thenReturn(7L);

            long count = messageService.unreadCount();
            assertEquals(7L, count);
            verify(messageMapper).selectCount(any());
        }
    }

    @Test
    @DisplayName("未读数量——无消息返回零")
    void unreadCount_zero() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(5L);
            when(messageMapper.selectCount(any())).thenReturn(0L);

            long count = messageService.unreadCount();
            assertEquals(0L, count);
        }
    }
}
