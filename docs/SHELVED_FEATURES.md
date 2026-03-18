# 已搁置功能说明

> 以下功能**前端代码已实现但暂不对外展示**，路由未注册，用户无法访问。
> **禁止删除相关文件**，待后续决策是否正式上线。
>
> 最后更新：2026-03-18

---

## 医护端

### 会诊管理

- **文件**：`frontend/src/views/medical/consultation/ConsultationPage.vue`
- **后端 API**：`GET/POST /medical/consultation`
- **功能**：发起会诊申请、查看会诊状态（待处理/已接受/已完成/已拒绝）
- **搁置原因**：社区卫生中心场景中会诊需求较少，暂不展示减少界面复杂度

---

### 转诊管理

- **文件**：`frontend/src/views/medical/referral/ReferralPage.vue`
- **后端 API**：`GET/POST /medical/referral`
- **功能**：开具转诊单、查看转诊状态（已开具/已到诊/已回转）
- **搁置原因**：转诊流程依赖外部医院对接，当前版本暂不实现跨院联动

---

### 工作量统计

- **文件**：`frontend/src/views/medical/stats/MyStatsPage.vue`
- **后端 API**：`GET /medical/stats/my`
- **功能**：本月接诊量、处方量统计，近 7 天接诊趋势 ECharts 柱状图
- **搁置原因**：管理员端数据看板已覆盖全局统计，医生个人维度统计暂缓

---

### 发药管理

- **文件**：`frontend/src/views/medical/dispense/DispensePage.vue`
- **后端 API**：`GET/POST /medical/dispense`（药房发药流程）
- **功能**：药房发药核对、发药记录查询
- **搁置原因**：当前处方审核由医护端处方页完成，独立发药窗口暂不启用

---

## 管理员端

### 绩效统计

- **文件**：`frontend/src/views/admin/performance/PerformancePage.vue`
- **后端 API**：`GET /admin/report/staff-performance`
- **功能**：按医生展示接诊量、处方量、随访量、平均评分
- **搁置原因**：绩效考核规则尚未确定，暂不对管理员展示

---

## 已废弃文件（可在适当时机清理）

| 文件 | 替代者 | 废弃原因 |
|------|--------|----------|
| `frontend/src/views/resident/follow-up/FollowUpPage.vue` | `resident/followup/ResidentFollowUpPage.vue` | 早期简化版，功能已被新版完全覆盖，调用的 API 路径已不存在 |

---

## 恢复上线步骤（供参考）

1. 在 `frontend/src/router/index.js` 对应角色的 `children` 中添加路由条目
2. 在 `AdminLayout.vue` / 侧边栏配置中添加菜单入口
3. 确认对应后端 Controller 接口正常（会诊/转诊需先完成后端业务逻辑）
