package com.chp.admin.service;

import com.chp.admin.entity.Drug;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.mapper.DrugMapper;
import com.chp.admin.mapper.DrugStockMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DrugService 单元测试")
class DrugServiceTest {

    @Mock private DrugMapper drugMapper;
    @Mock private DrugStockMapper drugStockMapper;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks private DrugService drugService;

    @Test
    @DisplayName("创建药品——返回含 ID 的实体")
    void createDrug_success() {
        Drug drug = new Drug();
        drug.setGenericName("阿莫西林胶囊");
        drug.setDrugCode("AMX-01");

        when(drugMapper.insert(any())).thenAnswer(inv -> {
            Drug d = inv.getArgument(0);
            d.setId(1L);
            return 1;
        });

        Drug result = drugService.createDrug(drug);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(drugMapper).insert(drug);
    }

    @Test
    @DisplayName("更新药品——调用 updateById")
    void updateDrug_success() {
        Drug drug = new Drug();
        drug.setId(1L);
        drug.setGenericName("阿莫西林胶囊（升级版）");

        when(drugMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> drugService.updateDrug(drug));
        verify(drugMapper).updateById(drug);
    }

    @Test
    @DisplayName("查询药品库存批次")
    void stockByDrug_success() {
        DrugStock stock = new DrugStock();
        stock.setDrugId(1L);
        stock.setQuantity(100);

        when(drugStockMapper.selectList(any())).thenReturn(List.of(stock));

        List<DrugStock> result = drugService.stockByDrug(1L);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getQuantity());
    }

    @Test
    @DisplayName("新增库存批次")
    void addStock_success() {
        DrugStock stock = new DrugStock();
        stock.setDrugId(1L);
        stock.setQuantity(200);

        when(drugStockMapper.insert(any())).thenAnswer(inv -> {
            DrugStock s = inv.getArgument(0);
            s.setId(10L);
            return 1;
        });

        DrugStock result = drugService.addStock(stock);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }
}
