# 前端 UI 改造交接文档

本文档用于把当前前端 UI 改造状态、下一步优先事项、文件级修改建议和可直接交给其他 AI 的执行提示整理成一份可复用材料。

适用范围：
- 当前仓库：`E:\ALL\gptcodex\NEW`
- 目标工程：`frontend`
- 参考原型：`ui-prototype`
- 不涉及后端功能改造，只聚焦前端视觉和交互重构

## 1. 当前改造结论

当前前端已经不是“未开始改造”的状态，而是进入了“设计系统已建立，核心页面正在铺开”的阶段。

建议这样判断完成度：

- 视觉基础层完成度：`75%-85%`
- 核心页面完成度：`50%-60%`
- 全站统一改造完成度：`55%-65%`

这说明最难的底层工作已经做了不少，但还没有把新风格彻底推到全部业务页。

## 2. 已完成得比较好的部分

### 2.1 全局设计系统

以下文件已经建立了新版设计语言的基础，不建议推翻重来，应继续复用：

- [frontend/src/styles/design-tokens.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/design-tokens.css)
- [frontend/src/styles/components.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/components.css)
- [frontend/src/styles/global.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/global.css)
- [frontend/src/styles/index.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/index.css)

这些文件已经具备：

- 主品牌色和语义色
- Element Plus 变量覆盖
- 状态标签、慢病标签、预警条、指标卡、步骤条
- 长者模式
- `focus-visible`
- 打印样式
- 桌面紧凑模式

### 2.2 全局 UI 状态管理

以下文件实现了敬老版和 header 高度同步，逻辑方向是正确的：

- [frontend/src/stores/uiStore.js](/E:/ALL/gptcodex/NEW/frontend/src/stores/uiStore.js)

已具备：

- `elderMode` 持久化到 `localStorage`
- 同步 `body.elder-mode`
- 动态同步 `--header-h`

### 2.3 已明显改造过的页面

以下页面已经不是原始版本，说明改造已经推进到页面层：

- [frontend/src/components/layout/ResidentLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/ResidentLayout.vue)
- [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)
- [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)
- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)
- [frontend/src/views/medical/workbench/WorkbenchPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/workbench/WorkbenchPage.vue)

## 3. 目前最明显的差距

### 3.1 导航与路由不一致

当前新版布局中，菜单入口少于路由定义，存在“页面还在，但用户进不去”的问题。

对照文件：

- [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)
- [frontend/src/router/index.js](/E:/ALL/gptcodex/NEW/frontend/src/router/index.js)

已定义但未完整出现在导航中的功能包括：

- 医护端：`/medical/vaccination`
- 管理端：`/admin/dept`
- 管理端：`/admin/vaccine-stock`
- 管理端：`/admin/dict`
- 管理端：`/admin/drug-log`
- 管理端：`/admin/schedule-transfer`

这是当前最优先要修的问题之一，因为它直接影响评审对“功能完整性”的判断。

### 3.2 管理驾驶舱还没有完全升级

以下页面仍然保留了较多旧后台结构：

- [frontend/src/views/admin/dashboard/DashboardPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dashboard/DashboardPage.vue)

当前问题：

- 还是传统 `el-row + el-card` 布局
- 只是换了新版配色，没有形成新版驾驶舱结构
- 还没充分使用 `metric-card-v2`
- 风险提醒、待审批、公告摘要的层次不够

### 3.3 医护工作台还有旧风格残留

以下页面结构已经变好，但细节还不统一：

- [frontend/src/views/medical/workbench/WorkbenchPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/workbench/WorkbenchPage.vue)

当前问题：

- 仍混用 emoji
- 有较多内联样式
- 体征录入和患者提醒区还可以进一步组件化
- 还没完全体现“临床工作台”的稳定感

### 3.4 登录页仍偏展示型

以下页面已经改过，但还没有完全收敛到机构化、政务医疗风格：

- [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)

当前问题：

- 左侧品牌区渐变偏重
- 装饰感偏强
- 演示账号直接暴露在主页面
- 更像“品牌展示页”，而不是“社区卫生服务中心统一入口”

### 3.5 居民首页还可以继续增强内容层级

以下页面已经比原版好很多，但还可以补“摘要与提醒”：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

当前问题：

- 快捷服务有了，但提醒层还不够
- 健康摘要感不足
- 快捷图标目前是字母/汉字缩写，不够系统化

## 4. 下一轮改造优先级

建议严格按下面顺序推进：

1. 修复布局与导航回归
2. 重做管理驾驶舱
3. 清理并收口医护工作台
4. 收敛登录页风格
5. 增强居民首页
6. 再扩展到预约、档案、处方、库存、字典、接种等业务页

## 5. 文件级修改建议

下面这部分可以直接拿来安排任务，或者交给其他 AI 逐个文件执行。

### 5.1 [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)

目标：

- 修复导航入口缺失
- 统一桌面侧边栏和移动底部导航的数据来源
- 减少后续路由新增时漏改菜单的问题

建议修改方式：

- 不再手写两套菜单
- 抽成 `medicalNavItems` 和 `adminNavItems` 配置数组
- 每个配置项包含：
  - `path`
  - `label`
  - `icon`
  - `visible`
- 侧边栏和移动端 tabbar 都从同一份数组 `v-for` 渲染

建议补齐的菜单项：

- 医护端：
  - 接种管理
- 管理端：
  - 科室管理
  - 疫苗库存
  - 数据字典
  - 出入库日志
  - 调班审批

附加建议：

- 如果后面继续扩展，可以把导航信息挪到路由 `meta.nav`，由路由自动派生菜单

### 5.2 [frontend/src/views/admin/dashboard/DashboardPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dashboard/DashboardPage.vue)

目标：

- 从“旧后台统计页”升级为“新版管理驾驶舱”

建议修改方向：

- 顶部标题区改成 `页面标题 + 副标题 + 右侧操作`
- 标题区下方增加一条 `alert-bar`
  - 展示药品预警
  - 待审批调班
  - 公告提醒
- 顶部统计卡改成 `metric-card-v2`
- 图表区保留现有接口，但调整布局层级
- “近期公告”和“药品 TOP5”改成独立摘要区
- 预警表格从单独的大表改成摘要卡 + 跳转入口

建议结构：

1. 页面头部
2. 风险提醒条
3. 四张指标卡
4. 趋势图 + 科室分布
5. 公告摘要 + 药品消耗 + 系统状态
6. 风险概览卡

代码层建议：

- 减少模板里直接写行内样式
- 颜色不要继续写死在 `statCards`
- 用 token 或语义 class 管理颜色

### 5.3 [frontend/src/views/medical/workbench/WorkbenchPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/workbench/WorkbenchPage.vue)

目标：

- 把当前较好的结构继续打磨成“统一的医护工作台样板页”

建议修改方向：

- 替换所有 emoji 为统一 SVG 或 `@element-plus/icons-vue`
- 患者状态、体征图标、风险提示统一风格
- 体征录入改为 schema 驱动渲染
- 减少模板中的内联样式
- 强化右侧待办区的信息价值

建议补充内容：

- 增加“待发药 / 待接种 / 待随访”小摘要
- 高风险提示条支持更明确的语义
- 当前患者卡增加更多关键信息，如过敏史、慢病标签、就诊编号

建议代码重构方式：

- 把体征字段提成数组，例如：
  - `bloodPressure`
  - `temperature`
  - `pulse`
  - `weight`
  - `spo2`
- 用 `v-for` 渲染输入项
- 使用全局 class：
  - `status-tag`
  - `chronic-tag`
  - `alert-bar`
  - `panel`
  - `panel-scroll-body`

### 5.4 [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)

目标：

- 从“展示型登录页”收敛到“机构入口页”

建议修改方向：

- 减弱左侧渐变和装饰图形
- 让视觉更像“社区卫生服务中心统一入口”
- 保留当前手机号/工号自动识别逻辑，不需要改登录流程
- 演示账号从主视图移到折叠帮助区或底部帮助链接

建议结构：

1. 左侧机构介绍区
2. 右侧登录表单区
3. 底部帮助入口

建议保留：

- 自动判断居民/医护/管理的逻辑
- 当前表单校验与跳转逻辑

建议收口：

- 减少投影和玻璃感
- 减少背景装饰
- 统一按钮与输入框气质

### 5.5 [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

目标：

- 从“简洁首页”升级成“居民服务首页”

建议修改方向：

- 继续保留长者模式切换
- 增加“今日提醒”或“健康摘要”区块
- 快捷服务图标替换为统一 SVG
- 增强首页的可读性和内容深度

建议新增区块：

- 今日提醒
  - 复诊提醒
  - 疫苗提醒
  - 消息提醒
- 健康摘要
  - 最近体征
  - 慢病标签
  - 最近就诊

建议保留：

- 当前 hero 结构
- 当前预约和消息数据获取逻辑
- 当前长者模式切换入口

### 5.6 [frontend/src/styles/design-tokens.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/design-tokens.css)

目标：

- 继续作为全局 token 唯一来源

建议修改方式：

- 新增 token 时优先加在这里
- 页面不要随意手写色值
- 如果某个页面需要新语义色，先提升到 token 层，再在页面中使用

### 5.7 [frontend/src/styles/components.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/components.css)

目标：

- 继续沉淀跨页面可复用的视觉语义类

建议新增方向：

- 页面头部容器
- 摘要区组件
- 图表卡标题区
- 统一操作栏
- 移动端底部安全区辅助类

## 6. 建议后续扩展的页面顺序

等上面 5 个核心文件改完后，再往下推进：

第二批建议页面：

- [frontend/src/views/resident/appointment/AppointmentPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/appointment/AppointmentPage.vue)
- [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue)
- [frontend/src/views/medical/prescription/PrescriptionPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/prescription/PrescriptionPage.vue)
- [frontend/src/views/medical/dispense/DispensePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/dispense/DispensePage.vue)
- [frontend/src/views/admin/drug/DrugStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/drug/DrugStockPage.vue)
- [frontend/src/views/admin/vaccine/VaccineStockPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/vaccine/VaccineStockPage.vue)
- [frontend/src/views/admin/dict/DictManagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dict/DictManagePage.vue)

## 7. 可直接交给其他 AI 的执行要求

下面这段可以直接给其他 AI 使用。

### 7.1 通用执行约束

请在 `E:\ALL\gptcodex\NEW\frontend` 中进行前端 UI 重构，要求如下：

- 不改后端接口协议
- 不删除现有业务逻辑
- 以现有 `frontend/src/styles/design-tokens.css`、`frontend/src/styles/components.css`、`frontend/src/styles/global.css` 为设计基础
- 参考 `E:\ALL\gptcodex\NEW\ui-prototype` 的视觉方向，但优先复用现有前端代码结构
- 尽量减少页面内硬编码色值和大段内联样式
- 优先提炼成可复用 class 或小组件
- 每次只改 1 到 2 个页面，改完后保证可运行

### 7.2 第一批执行任务

请优先完成以下 5 个文件的 UI 收口和重构：

- `frontend/src/components/layout/AdminLayout.vue`
- `frontend/src/views/admin/dashboard/DashboardPage.vue`
- `frontend/src/views/medical/workbench/WorkbenchPage.vue`
- `frontend/src/views/public/LoginPage.vue`
- `frontend/src/views/resident/HomePage.vue`

目标如下：

1. `AdminLayout.vue`
- 让侧边栏与移动端导航和路由定义保持一致
- 菜单项从配置数组渲染，避免手写重复
- 补齐缺失的医护和管理端菜单入口

2. `DashboardPage.vue`
- 改造成新版管理驾驶舱
- 使用统一指标卡和预警条
- 保留现有数据请求逻辑
- 优化布局层级和视觉统一性

3. `WorkbenchPage.vue`
- 去掉 emoji
- 统一状态和图标表达
- 减少内联样式
- 保留现有三栏结构与业务逻辑

4. `LoginPage.vue`
- 收敛为更克制的机构入口风格
- 保留现有登录逻辑
- 降低装饰感

5. `HomePage.vue`
- 增加提醒和摘要感
- 图标统一
- 保留长者模式和现有数据逻辑

### 7.3 输出要求

其他 AI 在执行时应输出：

- 修改了哪些文件
- 每个文件做了什么结构性调整
- 是否有潜在回归风险
- 是否需要继续扩展到下一批页面

## 8. 本轮改造的总体建议

当前不建议重新推翻前面的样式体系，因为地基已经搭得不错。更合理的路线是：

- 继续复用已建立的 token 和组件语义
- 先解决导航回归
- 把管理驾驶舱和医护工作台做成样板页
- 再按同样方式扩展到其他业务页

一句话总结：

当前前端改造已经完成了“底层统一”的关键阶段，下一步重点不是重做，而是把这套体系真正铺到每个核心页面。
