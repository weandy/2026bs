# 后续开发 TODO 清单

> Agent 读取本文件确定待开发项。每完成一项自动更新。
> 最后更新：2026-03-16

---

## P0 · 核心安全（预计 3 天）

### [P0-1] 居民个人中心完善
- [x] 后端：`PUT /resident/profile/password` 修改密码接口（旧密码验证 + 新密码复杂度 6-20 位字母+数字）
- [x] 后端：`PUT /resident/profile/phone` 修改手机号接口（需登录密码验证）
- [x] 后端：`GET /resident/profile` 获取个人信息接口
- [x] 前端：`ProfilePage.vue` 接入修改密码/手机号功能弹窗
- [x] 前端：退出登录按钮调用 `userStore.logout()` 并跳转 `/login`

### [P0-2] Caffeine 缓存集成
- [x] 引入 `caffeine` 依赖到 pom.xml
- [x] 创建 `CacheConfig.java` 配置 Bean
- [x] 科室列表缓存（maximumSize=50, TTL=24h）
- [x] 药品字典缓存（maximumSize=2000, TTL=24h），修改药品时 evict
- [x] 疫苗字典缓存（maximumSize=500, TTL=24h）
- [x] 系统配置缓存（maximumSize=100, TTL=1h）
- [x] 登录失败计数缓存（maximumSize=10000, TTL=30min），连续5次锁定

### [P0-3] 定时任务
- [x] 引入 `@EnableScheduling` 到 Starter
- [x] 预约过期自动取消：每小时扫描过了就诊日期仍 status=1 的预约 → status=4(过期)
- [x] 号源超时释放：预约创建后30分钟未确认 → 释放号源
- [x] 库存预警通知：每日凌晨扫描 drug_stock quantity < drug.alert_qty → 写入 notice

---

## P1 · 业务增强（预计 3 天）

### [P1-1] 统计报表后端
- [x] `GET /admin/report/overview` — 今日挂号数/就诊数/在线医生/药品预警数
- [x] `GET /admin/report/appointment` — 按日期/科室统计预约量趋势
- [x] `GET /admin/report/drug` — 按时间段统计药品消耗 TOP10
- [x] `POST /admin/report/export` — EasyExcel 导出报表（引入 easyexcel 依赖）

### [P1-2] 管理员仪表盘
- [x] 前端：`DashboardPage.vue` 接入真实统计 API 替换静态数据
- [x] 前端：使用 ECharts/Chart.js 展示预约趋势折线图
- [x] 前端：药品预警卡片展示

---

## P2 · 功能补全（预计 4 天）

### [P2-1] PDF 处方打印
- [x] 引入 `itext7-core` 依赖
- [x] 后端：`POST /medical/prescription/{id}/pdf` — 根据处方 ID 生成 PDF 流
- [x] 前端：处方管理页增加"打印"按钮，window.open 打开 PDF

### [P2-2] 调班申请审批
- [x] 确认 `schedule_transfer_request` 表已存在
- [x] 后端实体：`ScheduleTransferRequest.java`
- [x] 后端 Mapper + Service
- [x] `POST /admin/schedule/transfer` — 提交调班申请
- [x] `PUT /admin/schedule/transfer/{id}` — 审批（通过/驳回）
- [x] `GET /admin/schedule/transfer?page&size&status` — 申请列表
- [x] 前端：排班管理页增加"调班申请"Tab

### [P2-3] 随访趋势图表
- [x] 后端：`GET /medical/follow-up/trend/{planId}?limit=50` — 返回时序数据
- [x] 前端：`FollowUpPage.vue` 随访记录弹窗中增加 ECharts 折线图（血压/血糖趋势）

### [P2-4] 疫苗库存管理
- [x] 后端实体：`Vaccine.java`, `VaccineStock.java`
- [x] 后端：`POST /admin/vaccine/stock/in` — 入库
- [x] 后端：`POST /admin/vaccine/stock/out` — 出库
- [x] 后端：`GET /admin/vaccine?page&size` — 疫苗列表
- [x] 前端：管理端新增 VaccineStockPage.vue + 路由

---

## P3 · 体验优化（预计 4 天）

### [P3-1] 候诊公屏 WebSocket
- [x] 引入 `spring-boot-starter-websocket`
- [x] 后端：WebSocket 端点 `/ws/screen/{deptCode}`
- [x] 叫号时推送消息到公屏客户端
- [x] 前端 `PublicScreen.vue` 接入 WebSocket 实时刷新

### [P3-2] 前端 UI 美化
- [x] 管理端侧边栏导航高亮当前路由
- [x] 居民端底部 TabBar 导航
- [x] 响应式布局适配移动端
- [x] 为空状态添加友好插图
- [x] 页面过渡动画 `<transition>`

### [P3-3] 测试覆盖
- [x] 后端：核心 Service 单元测试（AuthService / AppointmentService / PrescriptionService）
- [x] 后端：Controller 集成测试（MockMvc）
- [x] 前端：Playwright E2E 测试脚本（登录流程 / 预约流程 / 管理操作）

---

## 完成记录

| 编号 | 完成日期 | 备注 |
|------|----------|------|
| P0-1 | 2026-03-15 | ProfileController + ProfilePage.vue |
| P0-2 | 2026-03-16 | CacheConfig + @Cacheable/@CacheEvict 4 个 Controller |
| P0-3 | 2026-03-15 | AppointmentExpireTask + StockAlertTask |
| P1-1 | 2026-03-16 | ReportService 8 接口 + appointment + export |
| P1-2 | 2026-03-16 | DashboardPage ECharts + 药品预警卡片 |
| P2-1 | 2026-03-15 | PrescriptionPdfService + iText7 |
| P2-2 | 2026-03-15 | ScheduleTransferService + Controller + Page |
| P2-3 | 2026-03-16 | FollowUpService.trend() + FollowUpPage ECharts |
| P2-4 | 2026-03-16 | VaccineStockService 完整 + 出库接口 |
| P3-1 | 2026-03-15 | WebSocketConfig + PublicScreen.vue |
| P3-2 | 2026-03-15 | AdminLayout + ResidentLayout 全面美化 |
| P3-3 | 2026-03-16 | 7 个 Service/Controller 测试 + 3 个 E2E 测试 |
