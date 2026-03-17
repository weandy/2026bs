package com.chp.medical.service;

import com.chp.admin.entity.FollowUpPlan;
import com.chp.admin.entity.FollowUpRecord;
import com.chp.admin.mapper.FollowUpPlanMapper;
import com.chp.admin.mapper.FollowUpRecordMapper;
import com.chp.admin.mapper.PublicHealthRecordMapper;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpService 单元测试")
class FollowUpServiceTest {

    @Mock private FollowUpPlanMapper planMapper;
    @Mock private FollowUpRecordMapper recordMapper;
    @Mock private PublicHealthRecordMapper publicHealthMapper;

    @InjectMocks private FollowUpService followUpService;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(10L, "李医生", "doctor", "QKMZ", "admin");
    }

    @Test
    @DisplayName("创建随访计划")
    void createPlan_success() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUserId).thenReturn(10L);
            when(planMapper.insert(any())).thenReturn(1);

            FollowUpPlan plan = new FollowUpPlan();
            plan.setResidentId(100L);
            plan.setChronicType("hypertension");

            FollowUpPlan result = followUpService.createPlan(plan);

            assertNotNull(result);
            assertEquals(1, result.getStatus());
            assertEquals(10L, result.getCreatedBy());
            verify(planMapper).insert(any());
        }
    }

    @Test
    @DisplayName("停止随访计划")
    void stopPlan_success() {
        FollowUpPlan plan = new FollowUpPlan();
        plan.setId(1L);
        plan.setStatus(1);
        when(planMapper.selectById(1L)).thenReturn(plan);
        when(planMapper.updateById(any())).thenReturn(1);

        followUpService.stopPlan(1L);

        assertEquals(2, plan.getStatus());
        verify(planMapper).updateById(plan);
    }

    @Test
    @DisplayName("停止不存在的计划 - 静默无操作")
    void stopPlan_notFound() {
        when(planMapper.selectById(99L)).thenReturn(null);

        followUpService.stopPlan(99L); // 不应抛异常

        verify(planMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("添加随访记录并更新下次随访日期")
    void addRecord_updateNextDate() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUserId).thenReturn(10L);
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            FollowUpPlan plan = new FollowUpPlan();
            plan.setId(1L);
            plan.setNextFollowDate(LocalDate.now());

            FollowUpRecord record = new FollowUpRecord();
            record.setPlanId(1L);
            record.setNextFollowDate(LocalDate.now().plusDays(30));

            when(recordMapper.insert(any())).thenReturn(1);
            when(planMapper.selectById(1L)).thenReturn(plan);
            when(planMapper.updateById(any())).thenReturn(1);

            FollowUpRecord result = followUpService.addRecord(record);

            assertNotNull(result);
            assertEquals(10L, result.getStaffId());
            assertEquals(LocalDate.now().plusDays(30), plan.getNextFollowDate());
            verify(planMapper).updateById(plan);
        }
    }

    @Test
    @DisplayName("随访趋势 - limit 越界修正")
    void trend_limitBoundary() {
        when(recordMapper.selectList(any())).thenReturn(java.util.List.of());

        // limit <= 0 应被修正为 50
        followUpService.trend(1L, -1);
        // limit > 200 应被修正为 50
        followUpService.trend(1L, 999);

        verify(recordMapper, times(2)).selectList(any());
    }
}
