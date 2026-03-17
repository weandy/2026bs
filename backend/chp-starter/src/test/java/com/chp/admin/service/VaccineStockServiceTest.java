package com.chp.admin.service;

import com.chp.admin.entity.VaccineStock;
import com.chp.admin.mapper.VaccineStockLogMapper;
import com.chp.admin.mapper.VaccineStockMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VaccineStockService 单元测试")
class VaccineStockServiceTest {

    @Mock private VaccineStockMapper vaccineStockMapper;
    @Mock private VaccineStockLogMapper vaccineStockLogMapper;

    @InjectMocks private VaccineStockService vaccineStockService;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(1L, "管理员", "admin", "ALL", "admin");
    }

    @Test
    @DisplayName("创建疫苗——自动设置 status=1")
    void create_success() {
        VaccineStock stock = new VaccineStock();
        stock.setVaccineName("新冠疫苗");
        stock.setVaccineCode("VCX-01");

        when(vaccineStockMapper.insert(any())).thenReturn(1);

        VaccineStock result = vaccineStockService.create(stock);
        assertEquals(1, result.getStatus());
        verify(vaccineStockMapper).insert(stock);
    }

    @Test
    @DisplayName("疫苗入库——数量增加并记录日志")
    void addStock_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            VaccineStock stock = new VaccineStock();
            stock.setId(1L);
            stock.setVaccineName("新冠疫苗");
            stock.setQuantity(100);

            when(vaccineStockMapper.selectById(1L)).thenReturn(stock);
            when(vaccineStockMapper.updateById(any())).thenReturn(1);
            when(vaccineStockLogMapper.insert(any())).thenReturn(1);

            vaccineStockService.addStock(1L, 50, "BATCH-001");

            assertEquals(150, stock.getQuantity());
            assertEquals("BATCH-001", stock.getBatchNo());
            verify(vaccineStockLogMapper).insert(any());
        }
    }

    @Test
    @DisplayName("疫苗入库失败——疫苗不存在")
    void addStock_notFound() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(vaccineStockMapper.selectById(99L)).thenReturn(null);
            assertThrows(BusinessException.class, () -> vaccineStockService.addStock(99L, 10, "B1"));
        }
    }

    @Test
    @DisplayName("疫苗出库成功——数量减少并记录日志")
    void removeStock_success() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            VaccineStock stock = new VaccineStock();
            stock.setId(1L);
            stock.setVaccineName("新冠疫苗");
            stock.setQuantity(100);
            stock.setBatchNo("B1");

            when(vaccineStockMapper.selectById(1L)).thenReturn(stock);
            when(vaccineStockMapper.updateById(any())).thenReturn(1);
            when(vaccineStockLogMapper.insert(any())).thenReturn(1);

            vaccineStockService.removeStock(1L, 30, "接种使用");

            assertEquals(70, stock.getQuantity());
            verify(vaccineStockLogMapper).insert(any());
        }
    }

    @Test
    @DisplayName("疫苗出库失败——库存不足")
    void removeStock_insufficientStock() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            sec.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            VaccineStock stock = new VaccineStock();
            stock.setId(1L);
            stock.setQuantity(5);

            when(vaccineStockMapper.selectById(1L)).thenReturn(stock);
            assertThrows(BusinessException.class, () -> vaccineStockService.removeStock(1L, 10, "出库"));
        }
    }
}
