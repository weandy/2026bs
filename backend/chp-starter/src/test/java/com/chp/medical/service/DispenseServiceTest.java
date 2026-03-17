package com.chp.medical.service;

import com.chp.admin.entity.DrugStock;
import com.chp.admin.mapper.DrugStockLogMapper;
import com.chp.admin.mapper.DrugStockMapper;
import com.chp.common.exception.BusinessException;
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
@DisplayName("DispenseService 单元测试")
class DispenseServiceTest {

    @Mock private PrescriptionMapper prescriptionMapper;
    @Mock private PrescriptionItemMapper prescriptionItemMapper;
    @Mock private DrugStockMapper drugStockMapper;
    @Mock private DrugStockLogMapper drugStockLogMapper;

    @InjectMocks private DispenseService dispenseService;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(20L, "王护士", "nurse", "YKFZ", "admin");
    }

    @Test
    @DisplayName("确认发药成功——扣减库存并更新处方状态")
    void confirmDispense_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(20L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            Prescription rx = new Prescription();
            rx.setId(1L);
            rx.setStatus(1);
            rx.setPrescNo("RX20260317001");

            PrescriptionItem item = new PrescriptionItem();
            item.setDrugId(100L);
            item.setDrugName("阿莫西林");
            item.setQuantity(2);
            item.setDrugUnit("盒");

            DrugStock stock = new DrugStock();
            stock.setId(10L);
            stock.setDrugId(100L);
            stock.setQuantity(50);
            stock.setStatus(1);

            when(prescriptionMapper.selectById(1L)).thenReturn(rx);
            when(prescriptionItemMapper.selectList(any())).thenReturn(List.of(item));
            when(drugStockMapper.selectList(any())).thenReturn(List.of(stock));
            when(drugStockMapper.updateById(any())).thenReturn(1);
            when(drugStockLogMapper.insert(any())).thenReturn(1);
            when(prescriptionMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> dispenseService.confirmDispense(1L));

            assertEquals(2, rx.getStatus());
            assertEquals(20L, rx.getPharmacistId());
            assertEquals(48, stock.getQuantity()); // 50 - 2
            verify(drugStockLogMapper).insert(any());
        }
    }

    @Test
    @DisplayName("确认发药失败——处方不存在")
    void confirmDispense_notFound() {
        when(prescriptionMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> dispenseService.confirmDispense(99L));
    }

    @Test
    @DisplayName("确认发药失败——处方非待发药状态")
    void confirmDispense_alreadyDispensed() {
        Prescription rx = new Prescription();
        rx.setId(1L);
        rx.setStatus(2); // 已发药

        when(prescriptionMapper.selectById(1L)).thenReturn(rx);
        assertThrows(BusinessException.class, () -> dispenseService.confirmDispense(1L));
    }

    @Test
    @DisplayName("确认发药失败——库存不足")
    void confirmDispense_insufficientStock() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(20L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            Prescription rx = new Prescription();
            rx.setId(1L);
            rx.setStatus(1);
            rx.setPrescNo("RX001");

            PrescriptionItem item = new PrescriptionItem();
            item.setDrugId(100L);
            item.setDrugName("阿莫西林");
            item.setQuantity(100);
            item.setDrugUnit("盒");

            DrugStock stock = new DrugStock();
            stock.setDrugId(100L);
            stock.setQuantity(5);
            stock.setStatus(1);

            when(prescriptionMapper.selectById(1L)).thenReturn(rx);
            when(prescriptionItemMapper.selectList(any())).thenReturn(List.of(item));
            when(drugStockMapper.selectList(any())).thenReturn(List.of(stock));
            when(drugStockMapper.updateById(any())).thenReturn(1);
            when(drugStockLogMapper.insert(any())).thenReturn(1);

            assertThrows(BusinessException.class, () -> dispenseService.confirmDispense(1L));
        }
    }

    @Test
    @DisplayName("驳回处方成功")
    void rejectDispense_success() {
        Prescription rx = new Prescription();
        rx.setId(1L);
        rx.setStatus(1);
        rx.setPrescNo("RX001");

        when(prescriptionMapper.selectById(1L)).thenReturn(rx);
        when(prescriptionMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> dispenseService.rejectDispense(1L, "药品过期"));
        assertEquals(3, rx.getStatus());
        assertEquals("药品过期", rx.getNotes());
    }

    @Test
    @DisplayName("驳回处方失败——非待发药状态")
    void rejectDispense_invalidStatus() {
        Prescription rx = new Prescription();
        rx.setId(1L);
        rx.setStatus(2);

        when(prescriptionMapper.selectById(1L)).thenReturn(rx);
        assertThrows(BusinessException.class, () -> dispenseService.rejectDispense(1L, "退回"));
    }
}
