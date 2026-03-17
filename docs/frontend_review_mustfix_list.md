# 前端 Review 必修修复清单

## 1. 文档用途

本文档基于当前前端代码的完整 review 结果整理，只保留最值得优先修复的问题。

目标是帮助团队快速完成最后一轮高价值收口，避免把时间继续花在低优先级美化上。

这份清单适用于：

- 组内分工修复
- 继续交给其他 AI 执行
- 答辩前的最后一轮质量收口

## 2. 当前结论

当前前端整体已经达到“可以展示、可以答辩、核心流程可跑”的状态，但仍存在几项不适合继续拖延的问题。

这些问题里，有些属于真实功能风险，有些属于系统一致性和维护风险。

建议优先修复下面 8 项。

## 3. 必修修复项

### 1. 修复前端路由权限拦截

文件：

- `frontend/src/router/index.js`

问题：

- `/admin` 路由已经声明只允许管理员访问
- 但前端守卫没有真正校验 `meta.roles`
- 当前医护角色仍可能通过直接输入地址访问管理员页面

风险：

- 前端权限边界失效
- 答辩或演示时容易被发现角色隔离不彻底

建议：

- 在路由守卫中正式读取并校验 `to.meta.roles`
- 对 doctor / nurse / admin 做清晰分流
- 非管理员访问 `/admin/*` 时直接重定向回 `/medical/workbench`

优先级：`P0`

### 2. 去掉管理驾驶舱中的伪数据

文件：

- `frontend/src/views/admin/dashboard/DashboardPage.vue`

问题：

- 今日接诊概况使用 `60/30/10` 比例前端硬算
- 医生工作量排行是写死的静态数据

风险：

- 管理端展示数据不可信
- 答辩时如果老师追问数据来源，会比较尴尬

建议：

- 有真实接口就接真实接口
- 如果暂时没有真实接口，就明确标记为“演示数据”
- 不要把伪数据混在正式统计区域里

优先级：`P0`

### 3. 修复驾驶舱错误 token

文件：

- `frontend/src/views/admin/dashboard/DashboardPage.vue`
- `frontend/src/styles/design-tokens.css`

问题：

- 页面里使用了 `--warning`
- 全局 token 实际定义的是 `--warn`

风险：

- “候诊中”指标高亮失效
- 样式表现依赖浏览器容错

建议：

- 统一改成 `--warn`
- 或者正式补一个 `--warning` 别名，但不建议继续扩大别名体系

优先级：`P0`

### 4. 统一残留页面的状态标签语义

文件：

- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- 其他仍在用旧状态类的页面

问题：

- 页面还在用 `success / dead`
- 全局 `status-tag` 已经统一成 `pending / in-progress / done / cancelled / absent`

风险：

- 状态颜色语义不一致
- 全站 UI 看起来“差一点统一”

建议：

- 残留页面全部切到全局状态体系
- 如果确实有新状态需求，就补到全局，不要只在单页里扩

优先级：`P1`

### 5. 补齐或替换悬空样式 token

文件：

- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/styles/design-tokens.css`

问题：

- 仍然使用 `--border-light`
- 仍然使用 `--primary-dark`
- 但全局 token 文件里没有正式定义

风险：

- 页面表现依赖默认回退
- 后续维护时容易继续误用

建议：

- 要么在 token 层正式定义
- 要么全部替换成已存在变量，例如 `--border`、`--primary-strong`

优先级：`P1`

### 6. 修复员工管理页“假筛选”

文件：

- `frontend/src/views/admin/user/StaffManagePage.vue`

问题：

- 页面有姓名 / 工号 / 手机号切换
- 但请求没有把 `searchType` 传给后端

风险：

- 用户以为筛选有效，实际上无效
- 这类“看起来能用但其实没用”的控件很伤质量感

建议：

- 如果后端支持，补上 `searchType`
- 如果后端暂不支持，就先移除该切换控件，保留统一搜索

优先级：`P1`

### 7. 第二梯队页面继续统一到设计系统

文件：

- `frontend/src/views/resident/visit-record/VisitRecordPage.vue`
- `frontend/src/views/resident/message/MessagePage.vue`
- `frontend/src/views/medical/followup/FollowUpPage.vue`
- `frontend/src/views/medical/schedule/MySchedulePage.vue`
- `frontend/src/views/admin/audit/AuditLogPage.vue`

问题：

- 这些页面仍有较多旧式 `el-page-header`
- 仍有页面私有头部、旧表单区和大量内联样式

风险：

- 全站统一度不够
- 响应式和视觉风格仍存在断层

建议：

- 统一接入 `page-header`、`panel`、`compact-form`、`custom-table`
- 顺手减少页面内联样式

优先级：`P2`

### 8. 增加基础回归测试链路

文件范围：

- `frontend/e2e/*.spec.js`

问题：

- 当前只有 3 个 E2E 文件
- 无法覆盖三端关键流程和本轮响应式改造风险

风险：

- 页面改得越多，回归风险越大
- 之后继续改动容易出现看不见的回退

建议至少补这几条：

- 登录
- 居民预约流程
- 医护工作台 -> 处方
- 管理端驾驶舱入口

优先级：`P2`

## 4. 推荐修复顺序

建议按这个顺序推进：

1. 路由权限拦截
2. 驾驶舱伪数据
3. token 错误与悬空 token
4. 状态标签统一
5. 员工管理假筛选
6. 第二梯队页面统一
7. E2E 回归补齐

## 5. 建议分工

### A 同学：权限与请求层

- `frontend/src/router/index.js`
- 必要时联动 `frontend/src/utils/request.js`

### B 同学：管理驾驶舱与管理端统一

- `frontend/src/views/admin/dashboard/DashboardPage.vue`
- `frontend/src/views/admin/dict/DictManagePage.vue`
- `frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- `frontend/src/views/admin/user/StaffManagePage.vue`

### C 同学：第二梯队页面统一

- `frontend/src/views/resident/visit-record/VisitRecordPage.vue`
- `frontend/src/views/resident/message/MessagePage.vue`
- `frontend/src/views/medical/followup/FollowUpPage.vue`

### D 同学：测试补齐

- `frontend/e2e/login.spec.js`
- `frontend/e2e/appointment.spec.js`
- 新增医护和管理端基础回归

## 6. 可直接交给其他 AI 的 Prompt

```text
请根据 E:\ALL\gptcodex\NEW\docs\frontend_review_mustfix_list.md 执行前端修复。

项目路径：
E:\ALL\gptcodex\NEW

要求：
- 不改后端接口协议
- 不删业务逻辑
- 优先修复高优先级真实问题
- 风格继续沿用现有设计系统
- 修改后保证 npm run build 通过

本轮重点：
1. 路由权限守卫补齐
2. 驾驶舱去掉伪数据
3. 修复错误 token 和悬空 token
4. 统一状态标签语义
5. 修复员工管理假筛选

输出要求：
- 先说明要怎么改
- 再直接改代码
- 最后总结：
  - 修改了哪些文件
  - 解决了哪些真实问题
  - 是否还有残余风险
```

## 7. 结论

现在前端已经不缺“看起来像一个完整系统”的感觉，真正还需要补的是：

- 权限边界
- 数据真实性
- 样式语义一致性
- 第二梯队页面统一度
- 基础回归测试

一句话总结：

这份清单做完之后，前端就会从“高完成展示版”更接近“稳定可交付版”。

