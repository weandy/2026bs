# 前端三角色响应式优化执行任务单

## 1. 文档用途

本文档是对 [frontend_responsive_optimization_plan.md](/E:/ALL/gptcodex/NEW/docs/frontend_responsive_optimization_plan.md) 的进一步拆解。

目标是把“整体优化计划”转成可以直接分给同学或其他 AI 执行的文件级任务单。

使用方式：

- 按阶段推进
- 按文件认领
- 每完成一项就做本地预览
- 优先保证答辩核心页面

## 2. 总体执行顺序

建议按下面顺序推进：

1. 先改布局壳层和全局样式
2. 再改答辩核心页面
3. 再补第二梯队业务页
4. 最后做答辩专项收口

## 3. 第一阶段：布局壳层与全局样式

### 任务 1：`frontend/src/components/layout/ResidentLayout.vue`

目标：

- 稳定居民端移动底部导航
- 校准页面内容区与底部 tabbar 的安全间距
- 保证 PC 端和移动端切换时内容不跳动

要改什么：

- 检查 `resident-content` 的滚动和底部留白
- 校准 `bottom-tabbar` 高度与内容区 `padding-bottom`
- 检查超小屏幕下 tab 文字是否拥挤
- 统一 active / hover / 默认态的点击反馈

验收标准：

- `375px` 下底部导航不遮挡内容
- 页面滚动自然
- 居民首页、预约页、消息页切换无布局抖动

优先级：`P0`

### 任务 2：`frontend/src/components/layout/AdminLayout.vue`

目标：

- 强化医护端和管理端在移动端的导航可达性
- 稳定顶部栏、主内容区、抽屉导航的行为

要改什么：

- 检查移动端 `4 主入口 + 更多` 是否覆盖常用场景
- 优化 `more-nav-list` 在小屏下的排列
- 检查 `el-main` 在移动端的 padding 和底部留白
- 检查抽屉中导航项过多时的滚动表现

验收标准：

- 医护端和管理端在手机宽度下有完整入口
- 抽屉打开后不会出现内容被遮挡或溢出
- 顶栏和底部导航同时存在时不冲突

优先级：`P0`

### 任务 3：`frontend/src/styles/global.css`

目标：

- 统一全局断点行为
- 继续补足移动端兜底样式

要改什么：

- 补充 `375px / 768px / 1024px` 下常见布局规则
- 加强对 `el-dialog`、`el-drawer`、`el-table` 的移动端兜底
- 避免页面继续各自维护零散的移动端补丁

验收标准：

- 常见弹窗、抽屉、表格在移动端有统一表现
- 页面不用再大量写重复媒体查询

优先级：`P0`

### 任务 4：`frontend/src/styles/components.css`

目标：

- 继续把页面级响应式结构沉淀为公共类

要改什么：

- 增加移动端列表卡、筛选工具栏、面板头部等公共类
- 继续扩充适用于“表格转卡片”的结构样式
- 给 `page-header`、`summary-grid`、`list-row`、`action-stack` 做更完整的断点适配

验收标准：

- 多个页面能直接复用公共结构类
- 页面内联响应式样式减少

优先级：`P0`

## 4. 第二阶段：答辩核心页面

## 4.1 居民端

### 任务 5：`frontend/src/views/public/LoginPage.vue`

目标：

- 保持桌面端完整品牌入口
- 移动端改成更稳定的纵向登录卡

要改什么：

- 检查移动端品牌区和表单区的上下间距
- 控制品牌区在手机端的高度，不要占屏过多
- 保证输入框、按钮在移动端点击区域充足

验收标准：

- PC 端像正式入口页
- 手机端不会出现顶部区域过高、表单被压缩

优先级：`P0`

### 任务 6：`frontend/src/views/resident/HomePage.vue`

目标：

- 居民首页在手机端达到最佳展示效果
- PC 端也具备完整摘要感

要改什么：

- 优化 `shortcut-grid` 在不同宽度下的列数
- 调整 Hero、提醒区、预约卡、消息卡的垂直节奏
- 检查长者模式下字号和卡片高度

验收标准：

- `375px` 下首页信息不拥挤
- 长者模式仍然自然
- PC 端不显得过空

优先级：`P0`

### 任务 7：`frontend/src/views/resident/appointment/AppointmentPage.vue`

目标：

- 让预约挂号在手机端完整可操作

要改什么：

- 强化移动端的单列布局
- 确保步骤条在小屏下不拥挤
- 优化医生卡、时段按钮、摘要卡的换行和宽度
- 优化成功页回执区域

验收标准：

- 手机宽度下可顺畅完成完整预约流程
- 时段按钮不挤压
- Stepbar 可读

优先级：`P0`

### 任务 8：`frontend/src/views/resident/health-record/HealthRecordPage.vue`

目标：

- 保证健康档案在手机端仍然层次清晰

要改什么：

- 把三列面板在小屏下改成顺序堆叠
- 优化指标卡、慢病标签、趋势图的上下节奏
- 保证 PDF 按钮不会压缩头部

验收标准：

- 小屏下不出现三列硬压缩
- 核心信息仍然清楚

优先级：`P0`

## 4.2 医护端

### 任务 9：`frontend/src/views/medical/workbench/WorkbenchPage.vue`

目标：

- 桌面端保持工作台感
- 移动端改成可演示的纵向流程

要改什么：

- 小屏下将队列区和接诊区改为上下结构
- 调整患者横条、体征栏、表单区的堆叠顺序
- 优化按钮区在移动端的排列

验收标准：

- PC 端仍是主展示形态
- 手机端能完整展示“队列 -> 接诊 -> 完成接诊”

优先级：`P0`

### 任务 10：`frontend/src/views/medical/prescription/PrescriptionPage.vue`

目标：

- 处方页在手机端从“宽表”转为“可读结构”

要改什么：

- 优化顶部筛选栏在小屏下的折行
- 表格视图在小屏下可以考虑转为卡片列表
- 抽屉和新建处方对话框在手机宽度下更稳定

验收标准：

- 小屏下可以查看处方和新建处方
- 不出现明显横向滚动失控

优先级：`P0`

### 任务 11：`frontend/src/views/medical/dispense/DispensePage.vue`

目标：

- 发药页兼顾桌面端核对效率和移动端演示稳定性

要改什么：

- 优化发药队列表格在小屏下的显示
- 抽屉核对表在移动端考虑减少列或转卡片摘要
- 优化拒单对话框与提示条布局

验收标准：

- 发药流程在手机宽度下可展示
- 核对信息仍可读

优先级：`P0`

## 4.3 管理端

### 任务 12：`frontend/src/views/admin/dashboard/DashboardPage.vue`

目标：

- 驾驶舱在 PC 端完整
- 移动端变成摘要化单列结构

要改什么：

- 调整指标卡在不同宽度下的排列
- 图表在小屏下改成单列堆叠
- 公告、预警、排行区在小屏下改成顺序展示

验收标准：

- PC 端仪表盘完整
- 手机端也能演示“有驾驶舱”

优先级：`P0`

### 任务 13：`frontend/src/views/admin/drug/DrugStockPage.vue`

目标：

- 药品库存页在移动端具备可展示的摘要视图

要改什么：

- 优化筛选区折行
- 表格在小屏下减少列或转卡片摘要
- 抽屉中的库存明细表做移动端兜底

验收标准：

- 手机宽度下不出现严重表格挤压
- 还能看懂库存、预警、批次

优先级：`P1`

### 任务 14：`frontend/src/views/admin/vaccine/VaccineStockPage.vue`

目标：

- 疫苗库存页与药品库存页保持同一套响应式策略

要改什么：

- 与药品库存页统一处理筛选区、表格、弹窗
- 预警看板对话框在移动端改为更稳定的标签页布局

验收标准：

- 与药品库存页观感一致
- 手机端不崩

优先级：`P1`

### 任务 15：`frontend/src/views/admin/dict/DictManagePage.vue`

目标：

- 数据字典页在移动端从树表转为更易读的列表摘要

要改什么：

- 优化筛选区和表格列数
- 必要时在小屏下改成“名称 + 编码 + 状态 + 操作”的摘要列表
- 弹窗表单在小屏下改成单列

验收标准：

- 不再是桌面树表硬压缩
- 操作入口仍清晰

优先级：`P1`

## 5. 第三阶段：第二梯队业务页

### 任务 16：`frontend/src/views/resident/visit-record/VisitRecordPage.vue`

目标：

- 从旧式 `page-header + el-card` 页提升为更统一的记录页

要改什么：

- 用全局 `page-header` 替换当前头部
- 减少内联样式
- 优化记录卡和详情弹窗在移动端的表现

优先级：`P1`

### 任务 17：`frontend/src/views/resident/message/MessagePage.vue`

目标：

- 统一消息页风格并优化移动端

要改什么：

- 替换页面私有头部为全局头部结构
- 调整 Tabs、消息卡、分页区
- 减少内联样式

优先级：`P1`

### 任务 18：`frontend/src/views/medical/followup/FollowUpPage.vue`

目标：

- 提升随访公卫页的桌面与移动端可读性

要改什么：

- 两个大模块在小屏下改成清晰堆叠
- 表格和对话框统一响应式
- 头部和按钮风格继续对齐

优先级：`P1`

### 任务 19：`frontend/src/views/medical/vaccination/VaccinationManagePage.vue`

目标：

- 接种管理在手机端也能稳定展示

要改什么：

- 优化列表、表单和对话框在小屏下的表现
- 与发药页、随访页的医护端风格靠拢

优先级：`P1`

### 任务 20：`frontend/src/views/medical/schedule/MySchedulePage.vue`

目标：

- 排班页在手机端避免复杂表格压缩

要改什么：

- 把周历或列表结构改成更易读形式
- 调班申请入口在移动端更突出

优先级：`P2`

### 任务 21：`frontend/src/views/admin/schedule/ScheduleManagePage.vue`

目标：

- 排班管理页维持桌面端最佳体验，移动端做摘要适配

要改什么：

- 优化筛选区、表格和审批入口
- 小屏下保留核心字段

优先级：`P2`

### 任务 22：`frontend/src/views/admin/audit/AuditLogPage.vue`

目标：

- 审计日志页在小屏下避免宽表不可读

要改什么：

- 筛选区与表格列做减法
- 重点保留时间、操作人、模块、结果

优先级：`P2`

## 6. 第四阶段：答辩专项整理

### 任务 23：统一答辩高频页面文案

涉及文件：

- `frontend/src/views/public/LoginPage.vue`
- `frontend/src/views/resident/HomePage.vue`
- `frontend/src/views/medical/workbench/WorkbenchPage.vue`
- `frontend/src/views/admin/dashboard/DashboardPage.vue`
- `frontend/src/views/medical/dispense/DispensePage.vue`

目标：

- 统一页面语气
- 避免过度口语化
- 让系统看起来更专业、更稳定

优先级：`P1`

### 任务 24：统一状态标签与 token 使用

涉及文件：

- `frontend/src/views/resident/appointment/AppointmentPage.vue`
- `frontend/src/views/medical/prescription/PrescriptionPage.vue`
- `frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/styles/design-tokens.css`
- `frontend/src/styles/components.css`

目标：

- 全站状态语义统一
- 去掉悬空 token

优先级：`P1`

### 任务 25：去内联样式专项整理

涉及文件：

- 第二阶段和第三阶段涉及的所有高频页面

目标：

- 继续减少 `style=""`
- 让公共样式层承担更多职责

优先级：`P2`

## 7. 推荐分工方式

### A 组：壳层与全局样式

- `ResidentLayout.vue`
- `AdminLayout.vue`
- `global.css`
- `components.css`

### B 组：居民端

- `LoginPage.vue`
- `HomePage.vue`
- `AppointmentPage.vue`
- `HealthRecordPage.vue`
- `VisitRecordPage.vue`
- `MessagePage.vue`

### C 组：医护端

- `WorkbenchPage.vue`
- `PrescriptionPage.vue`
- `DispensePage.vue`
- `FollowUpPage.vue`
- `VaccinationManagePage.vue`
- `MySchedulePage.vue`

### D 组：管理端

- `DashboardPage.vue`
- `DrugStockPage.vue`
- `VaccineStockPage.vue`
- `DictManagePage.vue`
- `ScheduleManagePage.vue`
- `AuditLogPage.vue`

## 8. 每个任务的统一验收标准

每个文件改完后，都建议至少检查以下几点：

- 在 `375px` 宽度下是否正常
- 在 `768px` 宽度下是否正常
- 在 `1280px` 宽度下是否正常
- 是否出现横向滚动
- 是否出现按钮或表单超出容器
- 是否沿用了全局 token 和组件样式
- 是否新增了太多页面私有样式

## 9. 可直接交给其他 AI 的任务模板

```text
请基于 E:\ALL\gptcodex\NEW\docs\frontend_responsive_file_tasklist.md 执行前端响应式优化。

项目路径：
E:\ALL\gptcodex\NEW

参考资料：
- docs/frontend_responsive_optimization_plan.md
- docs/frontend_responsive_file_tasklist.md
- frontend/src/styles/design-tokens.css
- frontend/src/styles/components.css
- frontend/src/styles/global.css

要求：
- 不改后端接口
- 不删业务逻辑
- 优先复用现有设计系统
- 尽量减少页面私有内联样式
- 优先保证答辩核心页面在 PC 和移动端都稳定展示

本轮只改以下文件：
[把文件路径填在这里]

输出要求：
- 先说明你准备如何改
- 再直接改代码
- 最后说明：
  - 修改了哪些布局点
  - 哪些断点行为被优化了
  - 还剩哪些待继续优化项
```

## 10. 结论

这份任务单的核心思路是：

- 不再大改视觉方向
- 直接围绕“PC + 移动端 + 答辩演示”做落地优化
- 让三端都变得更稳定、更完整、更适合展示

一句话总结：

后续优化要从“继续做样板页”转向“把所有关键页面做成真正可跨设备展示的成品页面”。

