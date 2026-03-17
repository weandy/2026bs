package com.chp.medical.service;

import com.chp.resident.entity.Prescription;
import com.chp.resident.entity.PrescriptionItem;
import com.chp.resident.mapper.PrescriptionItemMapper;
import com.chp.resident.mapper.PrescriptionMapper;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService 单元测试")
class PrescriptionServiceTest {

    @Mock private PrescriptionMapper prescriptionMapper;
    @Mock private PrescriptionItemMapper prescriptionItemMapper;

    @InjectMocks private PrescriptionService prescriptionService;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(10L, "李医生", "doctor", "QKMZ", "admin");
    }

    @Test
    @DisplayName("创建处方成功")
    void createPrescription_success() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUserId).thenReturn(10L);
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(prescriptionMapper.insert(any())).thenAnswer(inv -> {
                Prescription rx = inv.getArgument(0);
                rx.setId(1L); // 模拟 MyBatis-Plus 回填 ID
                return 1;
            });
            when(prescriptionItemMapper.insert(any())).thenReturn(1);

            PrescriptionItem item1 = new PrescriptionItem();
            item1.setDrugName("阿莫西林");
            item1.setQuantity(2);

            PrescriptionItem item2 = new PrescriptionItem();
            item2.setDrugName("布洛芬");
            item2.setQuantity(1);

            Prescription result = prescriptionService.createPrescription(1L, 100L, List.of(item1, item2));

            assertNotNull(result);
            assertTrue(result.getPrescNo().startsWith("RX"));
            assertEquals(1L, result.getVisitId());
            assertEquals(100L, result.getResidentId());
            assertEquals(1, result.getStatus());
            verify(prescriptionItemMapper, times(2)).insert(any());
        }
    }

    @Test
    @DisplayName("查询处方明细")
    void getItems_success() {
        when(prescriptionItemMapper.selectList(any())).thenReturn(List.of(new PrescriptionItem()));

        List<PrescriptionItem> items = prescriptionService.getItems(1L);

        assertNotNull(items);
        assertEquals(1, items.size());
    }
}
