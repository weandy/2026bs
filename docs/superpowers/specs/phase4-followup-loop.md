# Phase 4：创新点② 随访智能闭环

## 现状

| 组件 | 状态 | 说明 |
|------|------|------|
| `FollowUpPlan` 实体 | ✅ | chronicType, nextFollowDate, status, doctorId |
| `FollowUpRecord` 实体 | ✅ | BP/glucose/compliance/guidance/nextFollowDate |
| `FollowUpService` | ✅ | createPlan, addRecord, todayDuePlans, trend |
| `FollowUpController` | ✅ | 完整 CRUD + 今日到期 |
| `WorkbenchPage.vue` | ✅ | completeVisit() 已有 |

## 缺口（需新增）

1. **后端**: 接诊完成后自动检测慢病并建议随访计划（`WorkbenchService` 扩展）
2. **前端**: completeVisit 成功后弹出"随访计划确认"对话框（如果患者有慢病标签）
3. **前端**: 居民端就诊记录中显示关联的随访提醒

## 实现方案

### 后端：接诊完成接口返回随访建议

修改 `WorkbenchService.completeVisit()` 返回值，增加 `followUpSuggestion` 字段：
- 检查居民 `HealthRecord.chronicTags`
- 按病种匹配随访模板（硬编码模板，不额外建表）
- 返回建议的随访时间和重点问题

### 前端：WorkbenchPage 增加随访确认弹窗

`completeVisit()` 成功后：
1. 检查返回值中是否有 `followUpSuggestion`
2. 如果有，弹出确认框展示建议
3. 医生确认后自动调用 `POST /medical/follow-up/plan` 创建计划

### 前端：VisitRecordPage 增加随访提醒卡片

在就诊详情中查询关联的 FollowUpPlan，展示下次随访日期。

## 随访模板（硬编码）

| 病种 | 随访频率 | 建议下次时间 | 重点事项 |
|------|---------|------------|---------|
| hypertension | 每月 1 次 | 30 天后 | 监测血压，评估用药效果 |
| diabetes | 每月 1 次 | 30 天后 | 监测血糖，评估饮食控制 |
| chd | 每 2 月 1 次 | 60 天后 | 心电图复查，用药依从性 |
| copd | 每 3 月 1 次 | 90 天后 | 肺功能评估，用药调整 |
| stroke | 每月 1 次 | 30 天后 | 康复评估，预防复发 |
