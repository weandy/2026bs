# 前端 Code Review 报告

## 1. 说明

本文档基于当前 `frontend` 代码的完整审查结果整理。

审查范围包括：

- 路由与权限守卫
- 布局壳层
- 全局样式与设计 token
- 居民端、医护端、管理端核心页面
- 第二梯队业务页
- 请求层
- 构建结果

本次审查方式以静态代码 review 为主，并已实际执行一次前端构建：

- `npm run build` 通过

因此，本报告更侧重：

- 真实功能风险
- 权限与数据正确性问题
- 样式系统一致性问题
- 可维护性风险

## 2. 总体结论

当前前端已经达到“核心流程齐全、可展示、可答辩”的状态，但还没有完全进入“稳定收尾完成态”。

如果按研发视角判断：

- 前端功能页面完成度：90% - 95%
- UI 重构完成度：88% - 92%
- 全站统一度：80% - 85%
- 工程完成度：76% - 83%
- 综合前端完成度：86% - 90%

一句话概括：

前端主体已经完成，但仍有几项必须优先处理的高价值问题，尤其是权限拦截、驾驶舱伪数据和状态语义一致性。

## 3. Findings

### [P1] 前端路由没有真正限制医护角色访问管理员页面

文件：

- `frontend/src/router/index.js`

问题描述：

- `/admin` 路由已经声明 `roles: ['admin']`
- 但 `beforeEach` 守卫没有真正读取和校验 `to.meta.roles`
- 当前只做了“居民不能进后台”的域隔离
- 医护角色仍可能直接访问 `/admin/*`

影响：

- 前端权限边界不完整
- 用户可能先进入管理员页，再由接口 403 拦截
- 既影响安全感，也影响演示体验

建议：

- 在路由守卫中增加 `meta.roles` 判断
- `doctor / nurse` 访问 `/admin/*` 时直接跳回 `/medical/workbench`
- 保证前端层和后端权限策略一致

优先级：`P1`

### [P1] 管理驾驶舱存在伪数据，统计结果不可信

文件：

- `frontend/src/views/admin/dashboard/DashboardPage.vue`

问题描述：

- 今日接诊概况中的 `completed / waiting / cancelled` 不是来自真实接口
- 当前是按 `60 / 30 / 10` 比例由前端硬算
- 医生工作量排行也使用了静态模拟数据

影响：

- 管理驾驶舱数据不可靠
- 如果答辩或验收时被追问统计来源，风险较高
- 页面看起来完成度高，但管理数据不具备真实业务意义

建议：

- 优先接真实统计接口
- 如果暂时没有，就明确标记“演示数据”
- 不建议将伪数据直接混入正式统计卡和排行区

优先级：`P1`

### [P2] 驾驶舱等待数强调色失效，原因是错误 token

文件：

- `frontend/src/views/admin/dashboard/DashboardPage.vue`
- `frontend/src/styles/design-tokens.css`

问题描述：

- 页面中 `text-warn` 使用了 `var(--warning)`
- 但全局 token 定义的是 `--warn`

影响：

- “候诊中”指标不会显示预期的警示色
- 样式结果依赖浏览器回退

建议：

- 统一改为 `var(--warn)`
- 或者正式补一个别名 token，但更建议保持 token 简洁

优先级：`P2`

### [P2] 数据字典页仍依赖未定义 token

文件：

- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/styles/design-tokens.css`

问题描述：

- 页面仍使用 `--border-light`
- 页面仍使用 `--primary-dark`
- 但全局 token 中没有定义这两个变量

影响：

- 页面边框和分类标签颜色表现不稳定
- 设计系统被绕开
- 容易继续扩散类似问题

建议：

- 正式补齐这两个 token
- 或直接替换成现有 token，例如：
  - `--border`
  - `--primary-strong`

优先级：`P2`

### [P2] 疫苗库存页和数据字典页仍在使用旧状态标签语义

文件：

- `frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/styles/components.css`

问题描述：

- 页面仍在使用 `success / dead`
- 但当前全局 `status-tag` 已统一成：
  - `pending`
  - `in-progress`
  - `done`
  - `cancelled`
  - `absent`

影响：

- 状态标签视觉语义不统一
- 页面看起来“基本统一”，但细节仍然脱节

建议：

- 将残留页面统一到全局 `status-tag` 体系
- 如果确实需要新状态，正式补到全局组件层，而不是只在单页使用

优先级：`P2`

### [P2] 员工管理页的搜索类型选择器没有实际生效

文件：

- `frontend/src/views/admin/user/StaffManagePage.vue`

问题描述：

- 页面提供“姓名 / 工号 / 手机号”搜索类型切换
- 但请求中只传了 `keyword`
- `searchType` 没有参与查询参数

影响：

- 这是典型的“看起来有功能，实际上无效”
- 对用户和评审都不友好

建议：

- 如果后端支持，补上传参
- 如果后端不支持，直接移除该切换控件，保留统一搜索框

优先级：`P2`

### [P3] 第二梯队页面还没有完全接入新的设计系统

文件示例：

- `frontend/src/views/resident/visit-record/VisitRecordPage.vue`
- `frontend/src/views/resident/message/MessagePage.vue`
- `frontend/src/views/medical/followup/FollowUpPage.vue`
- `frontend/src/views/medical/schedule/MySchedulePage.vue`
- `frontend/src/views/admin/audit/AuditLogPage.vue`

问题描述：

- 这些页面仍大量保留旧式 `el-page-header`
- 仍有页面私有头部结构
- 内联样式较多
- 与新的 `page-header / panel / compact-form / custom-table` 体系衔接不够

影响：

- 全站统一度被拉低
- 维护成本继续偏高
- 响应式适配会越来越难统一

建议：

- 第二梯队页面继续接入公共结构类
- 顺手减少内联样式
- 不需要重写页面，但建议收一轮旧写法

优先级：`P3`

### [P3] 前端测试覆盖不足，难以支撑这轮 UI 与响应式改造回归

文件范围：

- `frontend/e2e/*.spec.js`

问题描述：

- 当前仅看到 3 个 Playwright E2E 文件
- 覆盖范围不足以支撑三端主要流程

影响：

- 页面改得越多，回归风险越大
- 响应式与布局回退较难及时发现

建议：

- 至少补充：
  - 登录
  - 居民预约
  - 医护工作台到处方
  - 管理端驾驶舱入口

优先级：`P3`

## 4. 正向评价

本次 review 里也有不少明显做得好的地方。

### 4.1 设计系统已经成型

以下文件已经具备较完整的体系化能力：

- `frontend/src/styles/design-tokens.css`
- `frontend/src/styles/components.css`
- `frontend/src/styles/global.css`

说明：

- token、状态标签、指标卡、告警条、公共头部、表单与抽屉结构都已经比较清晰

### 4.2 核心页面质量较高

以下页面已经具备很强的展示与答辩能力：

- `frontend/src/views/public/LoginPage.vue`
- `frontend/src/views/resident/HomePage.vue`
- `frontend/src/views/resident/appointment/AppointmentPage.vue`
- `frontend/src/views/resident/health-record/HealthRecordPage.vue`
- `frontend/src/views/medical/workbench/WorkbenchPage.vue`
- `frontend/src/views/medical/prescription/PrescriptionPage.vue`
- `frontend/src/views/medical/dispense/DispensePage.vue`
- `frontend/src/views/admin/dashboard/DashboardPage.vue`

### 4.3 布局壳层方向是对的

以下文件说明你们在三端壳层上已经走到比较成熟的阶段：

- `frontend/src/components/layout/ResidentLayout.vue`
- `frontend/src/components/layout/AdminLayout.vue`

尤其是：

- 居民端底部导航
- 医护 / 管理端移动导航抽屉
- 顶栏和内容区组织

这些已经不是“脚手架默认态”了。

## 5. 构建与工程现状

### 构建结果

- `npm run build` 通过

### 仍存在的工程信号

- `vendor-ui` 和 `vendor-echarts` chunk 仍然较大
- 说明后续如果继续追求加载体验，还需要做一次拆包与性能收口

### 维护信号

- 代码里仍有不少内联样式残留
- 虽然不再是阻塞问题，但说明“工程收口”还没完全结束

## 6. 建议修复顺序

建议按下面顺序推进：

1. 修路由权限守卫
2. 去掉驾驶舱伪数据
3. 修 token 错误和悬空 token
4. 统一残留状态标签语义
5. 修员工管理假筛选
6. 收第二梯队页面
7. 补基础回归测试

## 7. 最终结论

这套前端已经不需要再做“大重构”，真正需要的是最后一轮高价值修复与收口。

当前最准确的判断是：

- 已经具备较强的展示与答辩能力
- 设计系统和核心页面已经基本完成
- 仍有几处不适合忽略的真实问题

一句话总结：

前端已经接近完成，但还没有到“可以完全放心收尾”的程度；优先把权限、伪数据、token 和状态语义这几项修掉，整体质量会明显再上一个台阶。

