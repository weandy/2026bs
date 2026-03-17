package com.chp.controller;

import com.chp.admin.controller.DeptController;
import com.chp.admin.entity.Dept;
import com.chp.admin.mapper.DeptMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DeptController 集成测试（MockMvc 独立模式）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeptController 集成测试")
class DeptControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DeptMapper deptMapper;

    @InjectMocks
    private DeptController deptController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deptController).build();
    }

    @Test
    @DisplayName("GET /admin/dept/list 成功")
    void listDepts_success() throws Exception {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setDeptCode("QKMZ");
        dept.setDeptName("全科门诊");
        when(deptMapper.selectList(any())).thenReturn(List.of(dept));

        mockMvc.perform(get("/admin/dept/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].deptCode").value("QKMZ"));
    }

    @Test
    @DisplayName("POST /admin/dept 新增科室")
    void createDept_success() throws Exception {
        when(deptMapper.insert(any(Dept.class))).thenReturn(1);

        Dept dept = new Dept();
        dept.setDeptCode("EKMZ");
        dept.setDeptName("儿科门诊");

        mockMvc.perform(post("/admin/dept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptCode").value("EKMZ"));
        verify(deptMapper).insert(any());
    }

    @Test
    @DisplayName("PUT /admin/dept/{id} 更新科室")
    void updateDept_success() throws Exception {
        when(deptMapper.updateById(any(Dept.class))).thenReturn(1);

        Dept dept = new Dept();
        dept.setDeptName("全科门诊（更新）");

        mockMvc.perform(put("/admin/dept/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk());
        verify(deptMapper).updateById(any());
    }

    @Test
    @DisplayName("DELETE /admin/dept/{id} 删除科室")
    void deleteDept_success() throws Exception {
        when(deptMapper.deleteById(1L)).thenReturn(1);

        mockMvc.perform(delete("/admin/dept/1"))
                .andExpect(status().isOk());
        verify(deptMapper).deleteById(1L);
    }
}
