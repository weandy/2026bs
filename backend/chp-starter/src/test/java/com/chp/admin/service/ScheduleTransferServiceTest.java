package com.chp.admin.service;

import com.chp.admin.entity.ScheduleTransferRequest;
import com.chp.admin.mapper.ScheduleTransferRequestMapper;
import com.chp.common.exception.BusinessException;
import com.chp.security.filter.JwtUserDetails;
import com.chp.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("ScheduleTransferService 单元测试")
class ScheduleTransferServiceTest {

    @Mock private ScheduleTransferRequestMapper transferMapper;

    @InjectMocks private ScheduleTransferService service;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(10L, "王医生", "doctor", "QKMZ", "admin");
    }

    @Test
    @DisplayName("提交调班申请——自动填充当前用户")
    void submit_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(10L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(transferMapper.insert(any())).thenReturn(1);

            ScheduleTransferRequest req = new ScheduleTransferRequest();
            req.setScheduleId(100L);
            req.setTargetStaffId(20L);
            req.setTargetStaffName("李医生");
            req.setReason("私事");

            assertDoesNotThrow(() -> service.submit(req));
            assertEquals(10L, req.getStaffId());
            assertEquals("王医生", req.getStaffName());
            assertEquals(0, req.getStatus());
            verify(transferMapper).insert(req);
        }
    }

    @Test
    @DisplayName("审批通过——正确更新状态和审批信息")
    void review_approve_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(new JwtUserDetails(1L, "管理员", "admin", "ALL", "admin"));

            ScheduleTransferRequest req = new ScheduleTransferRequest();
            req.setId(5L);
            req.setStatus(0); // 待审批
            when(transferMapper.selectById(5L)).thenReturn(req);
            when(transferMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> service.review(5L, 1, "同意"));
            assertEquals(1, req.getStatus());
            assertEquals("同意", req.getReviewComment());
            assertEquals(1L, req.getReviewerId());
        }
    }

    @Test
    @DisplayName("审批失败——申请不存在")
    void review_notFound() {
        when(transferMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.review(99L, 1, "ok"));
    }

    @Test
    @DisplayName("审批失败——已审批不可重复操作")
    void review_alreadyReviewed() {
        ScheduleTransferRequest req = new ScheduleTransferRequest();
        req.setId(5L);
        req.setStatus(1); // 已批准
        when(transferMapper.selectById(5L)).thenReturn(req);

        assertThrows(BusinessException.class, () -> service.review(5L, 2, "拒绝"));
    }
}
