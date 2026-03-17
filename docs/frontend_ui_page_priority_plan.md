# 前端业务页面改造优先级任务单

本文档用于在当前前端 UI 已基本收口的基础上，继续推进第二批业务页面改造。

目标：

- 让新版设计系统真正覆盖核心业务流程页
- 优先改造高频页面和闭环页面
- 避免无序推进造成返工

适用范围：

- 项目路径：`E:\ALL\gptcodex\NEW`
- 前端路径：`E:\ALL\gptcodex\NEW\frontend`

## 1. 总体排序

建议按以下顺序推进：

1. [frontend/src/views/resident/appointment/AppointmentPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/appointment/AppointmentPage.vue)
2. [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue)
3. [frontend/src/views/medical/prescription/PrescriptionPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/prescription/PrescriptionPage.vue)
4. [frontend/src/views/medical/dispense/DispensePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/dispense/DispensePage.vue)
5. [frontend/src/views/admin/drug/DrugStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/drug/DrugStockPage.vue)
6. [frontend/src/views/admin/vaccine/VaccineStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/vaccine/VaccineStockPage.vue)
7. [frontend/src/views/admin/dict/DictManagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dict/DictManagePage.vue)

排序原则：

- 先居民端高频主流程
- 再医护端核心闭环
- 最后管理端支撑页面

## 2. 页面级任务说明

### 2.1 预约挂号页

文件：

- [frontend/src/views/resident/appointment/AppointmentPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/appointment/AppointmentPage.vue)

优先级：`P0`

原因：

- 这是居民端最核心的高频流程页
- 最能体现前端体验是否真正升级
- 改好后可以直接提升演示和答辩观感

改造目标：

- 做成清晰的多步骤预约流程
- 强化“选科室 -> 选医生/时段 -> 填信息 -> 确认完成”的引导感
- 提升移动端可读性和点按体验

建议重点：

- 复用 `stepbar`
- 复用 `slot-btn`
- 复用 `panel`
- 把当前流程切换做得更像正式服务流程，不只是表单拼接
- 增加“预约摘要卡”

验收标准：

- 步骤状态清楚
- 时段选择反馈明确
- 提交前有清晰确认区
- 移动端操作不拥挤

### 2.2 健康档案页

文件：

- [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue)

优先级：`P0`

原因：

- 和居民首页的健康摘要是天然联动的
- 这是最适合把慢病标签、健康指标、摘要卡真正用起来的页面

改造目标：

- 从“信息展示页”升级成“结构化健康档案页”
- 强化关键信息的摘要感

建议重点：

- 复用 `chronic-tag`
- 复用 `indicator-badge`
- 使用摘要卡、分组块、信息卡替代单纯堆字段
- 加强生命体征与病史分区

验收标准：

- 血压、血糖、体重、慢病等关键信息一眼能看清
- 页面不只是 descriptions 或简单列表
- 和居民首页的健康摘要视觉语言一致

### 2.3 处方管理页

文件：

- [frontend/src/views/medical/prescription/PrescriptionPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/prescription/PrescriptionPage.vue)

优先级：`P0`

原因：

- 医护工作台之后最关键的业务页
- 改好后医护主流程能真正串起来

改造目标：

- 让处方页看起来像临床工作区，而不是普通后台表格页
- 强化患者上下文、处方状态和操作区

建议重点：

- 复用 `page-header`
- 复用 `status-tag`
- 复用 `panel`
- 页面拆成“患者信息 / 处方录入 / 处方预览”
- 突出处方状态、打印入口、药品明细层次

验收标准：

- 医生能快速理解当前患者与处方关系
- 页面层次清楚
- 处方状态表达统一

### 2.4 药房发药页

文件：

- [frontend/src/views/medical/dispense/DispensePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/dispense/DispensePage.vue)

优先级：`P0`

原因：

- 这是设计需求里关键的闭环页
- 和处方页一起改，收益最大

改造目标：

- 做成标准的“待发药列表 + 处方校对 + 发药确认”页面
- 提升状态感和核对感

建议重点：

- 复用 `status-tag`
- 复用 `panel-scroll-body`
- 复用 `list-row`
- 强调患者信息、处方号、药品清单和批次确认
- 让“确认发药 / 缺药处理 / 异常处理”更清楚

验收标准：

- 发药流程一眼可理解
- 处方状态与库存状态表达清楚
- 页面不再像普通后台列表页

### 2.5 药品库存页

文件：

- [frontend/src/views/admin/drug/DrugStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/drug/DrugStockPage.vue)

优先级：`P1`

原因：

- 管理端高频页
- 能把新版管理驾驶舱风格继续延伸到业务后台

改造目标：

- 让页面从“库存 CRUD 页”升级成“库存管理工作页”

建议重点：

- 复用 `page-header`
- 复用 `summary-grid`
- 复用 `list-row`
- 增加预警摘要、批次信息、效期提示
- 强化筛选区和摘要区

验收标准：

- 低库存、临期、异常项目更醒目
- 页面层次清楚，不只是一张表

### 2.6 疫苗库存页

文件：

- [frontend/src/views/admin/vaccine/VaccineStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/vaccine/VaccineStockPage.vue)

优先级：`P1`

原因：

- 和药品库存属于同类场景
- 适合复用一整套管理页结构

改造目标：

- 建立与药品库存页一致的管理体验
- 让疫苗库存页面有独立业务识别度

建议重点：

- 与药品库存共用结构，不重复造轮子
- 重点突出批次、效期、预警、接种去向

验收标准：

- 和药品库存风格统一
- 但业务信息区别足够清楚

### 2.7 数据字典页

文件：

- [frontend/src/views/admin/dict/DictManagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dict/DictManagePage.vue)

优先级：`P2`

原因：

- 它对整体一致性有帮助
- 但优先级低于高频主流程页

改造目标：

- 提升后台基础维护页的专业感和一致性

建议重点：

- 复用 `page-header`
- 复用 `panel`
- 复用 `list-row`
- 让不同字典类型的切换、筛选和操作区更清楚

验收标准：

- 不再像默认后台管理页
- 与管理驾驶舱和库存页风格统一

## 3. 每一批次建议

### 第一批

- `AppointmentPage.vue`
- `HealthRecordPage.vue`

目标：

- 先把居民端体验做成一套完整闭环

### 第二批

- `PrescriptionPage.vue`
- `DispensePage.vue`

目标：

- 打通医护端核心工作流

### 第三批

- `DrugStockPage.vue`
- `VaccineStockPage.vue`
- `DictManagePage.vue`

目标：

- 完成管理端业务页统一

## 4. 本轮推进建议

最稳妥的推进方式：

1. 每次只改 1 到 2 个页面
2. 每改完一页就看是否能进一步提炼全局样式
3. 优先复用现有：
   - `design-tokens.css`
   - `components.css`
   - `global.css`
4. 页面不要继续堆私有动画和私有色值

## 5. 可直接交给其他 AI 的 Prompt

```text
请继续推进 E:\ALL\gptcodex\NEW\frontend 的业务页面 UI 改造。

参考资料：
- E:\ALL\gptcodex\NEW\docs\frontend_ui_refactor_handoff.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_review_round2.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_tasklist_round4_mustfix.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_page_priority_plan.md
- E:\ALL\gptcodex\NEW\ui-prototype

要求：
- 不改后端接口
- 不删业务逻辑
- 优先复用现有设计系统
- 不要再新增过多页面私有动画和私有色值
- 风格保持医疗政务风：可信、克制、清爽、专业

请按以下顺序推进：
1. frontend/src/views/resident/appointment/AppointmentPage.vue
2. frontend/src/views/resident/health-record/HealthRecordPage.vue
3. frontend/src/views/medical/prescription/PrescriptionPage.vue
4. frontend/src/views/medical/dispense/DispensePage.vue
5. frontend/src/views/admin/drug/DrugStockPage.vue
6. frontend/src/views/admin/vaccine/VaccineStockPage.vue
7. frontend/src/views/admin/dict/DictManagePage.vue

每次建议只改 1 到 2 个页面。

输出要求：
- 先说明当前页面存在的问题
- 再直接修改代码
- 最后总结：
  - 修改了哪些文件
  - 复用了哪些全局样式
  - 是否需要把某些页面样式进一步提升到公共层
```

## 6. 一句话总结

下一阶段的重点不是继续做壳层，而是把新版风格真正铺到居民端、医护端和管理端的核心业务页面上。
