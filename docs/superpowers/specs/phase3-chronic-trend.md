# Phase 3：创新点① 慢病健康趋势追踪

## 现状

| 组件 | 已有 | 状态 |
|------|------|------|
| `HealthVital` 实体 | id/residentId/vitalType/vitalValue/measureTime/note/createdAt | ❌ 缺 recordedBy |
| 居民端 API | `POST/GET/GET-latest /resident/vital` | ✅ 可用 |
| 医护端 API | `GET/PUT /medical/health-record/{residentId}` | ❌ 缺录入指标接口 |
| ECharts 趋势图 | `HealthRecordPage.vue` 已有 vue-echarts 折线图 | 🟡 使用 physicalExam JSON，应改用 health_vital 表 |
| 异常预警 | 无 | ❌ 缺失 |

## 改动清单

### 后端（3 个文件）

1. **`HealthVital.java`** — 增加 `recordedBy` (Long) 和 `recordedByName` (String) 字段
2. **[NEW] `MedicalVitalController.java`** — 医护端录入指标 POST `/medical/vital/{residentId}`
3. **[NEW] `VitalAnomalyService.java`** — 异常检测：连续 3 次超阈值时在 follow_up_plan 中标记

### 前端（2 个文件）

4. **`HealthRecordPage.vue`** — 改用 `/resident/vital?type=xxx` 获取趋势数据而非 physicalExam JSON
5. **`RecordManagePage.vue`** — 在居民档案详情中加入"录入指标"按钮和表单弹窗

### 数据库

6. `ALTER TABLE health_vital ADD COLUMN recorded_by BIGINT, ADD COLUMN recorded_by_name VARCHAR(50)`

## 异常检测规则

| 指标 | 阈值 | 连续次数 | 动作 |
|------|------|---------|------|
| 收缩压 | > 140 mmHg | 3 | 标记异常 |
| 舒张压 | > 90 mmHg | 3 | 标记异常 |
| 空腹血糖 | > 7.0 mmol/L | 3 | 标记异常 |
| 空腹血糖 | < 3.9 mmol/L | 2 | 标记异常 |
