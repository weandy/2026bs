# Phase 1：路由清理 — 移除冗余模块

## 目标

根据 `system-design-spec-v1.md`，从前端路由和导航中移除不再需要的模块。**不删除后端代码和 Vue 文件**，仅使其"不可达"。

## 被移除清单

### 居民端（移除 3 个）

| 模块 | 路由 | 原因 |
|------|------|------|
| 消息通知 | `/resident/message` | 合并到各页面内提醒 |
| 健康资讯 | `/resident/article` | 纯信息展示，砍掉 |
| 我的随访 | `/resident/follow-up` | 合并到就诊记录页面 |

> 保留 `/resident/queue-progress`（候诊进度），因为它参与诊疗主线第3步。

### 医护端（移除 4 个）

| 模块 | 路由 | 原因 |
|------|------|------|
| 药房发药 | `/medical/dispense` | 独立 ERP 域 |
| 会诊管理 | `/medical/consultation` | 与转诊重叠 |
| 转诊管理 | `/medical/referral` | 社区极少用 |
| 工作量统计 | `/medical/my-stats` | 砍掉 |

### 管理端（移除 9 个）

| 模块 | 路由 | 原因 |
|------|------|------|
| 药品库存 | `/admin/drug` | 独立 ERP 域 |
| 出入库日志 | `/admin/drug-log` | 附属于药品 |
| 疫苗库存 | `/admin/vaccine-stock` | 独立域 |
| 审计日志 | `/admin/audit-log` | 运维功能 |
| 数据字典 | `/admin/dict` | 配置功能 |
| 系统配置 | `/admin/config` | 运维功能 |
| 文章管理 | `/admin/article` | 健康资讯一起砍 |
| 调班审批 | `/admin/schedule-transfer` | 合并进排班管理 |
| 医生绩效 | `/admin/performance` | 砍掉 |

## 改动文件清单

1. `frontend/src/router/index.js` — 注释或移除路由条目
2. `frontend/src/components/layout/AdminLayout.vue` — 从 medicalNav / adminNav 中移除
3. `frontend/src/components/layout/ResidentLayout.vue` — 更新 tabs（加入候诊进度等保留模块）
4. `frontend/src/views/resident/HomePage.vue` — 移除被砍模块的快捷入口

## 验证方式

- 编译无报错
- 访问被砍路由应显示 404
- 各端导航中不再出现被砍条目
