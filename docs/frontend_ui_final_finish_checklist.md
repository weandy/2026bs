# 前端 UI 最终收尾清单

## 1. 当前判断

当前这套前端已经可以视为“第一阶段 UI 重构基本完成”。

从代码状态看，整体已经具备以下特点：

- 全局设计系统已经成型，颜色、间距、状态标签、指标卡、页面头部这些基础能力比较稳定。
- 居民端、医护端、管理端都已经有样板页，不再是只有首页做得好看。
- 导航、移动端可达性、长者模式、页面分层这些关键 UX 问题基本都处理过了。
- 现在剩下的问题已经不是“大方向不对”，而是最后一轮语义统一和工程收口。

如果按展示和答辩标准判断，当前前端已经够用；如果按“做得更完整、更利于后续维护”判断，还值得再做一轮轻量优化。

## 2. 仍建议继续优化的问题

### P1. 状态标签语义还没有完全统一

目前全局 `status-tag` 体系已经比较明确，但个别业务页仍在用旧的页面私有状态名。

重点检查文件：

- `frontend/src/views/resident/appointment/AppointmentPage.vue`
- `frontend/src/views/medical/prescription/PrescriptionPage.vue`

当前问题：

- 预约页的历史预约状态已经基本对齐全局语义，但医生可约状态仍然在使用 `success / dead`。
- 处方页的发药状态仍然在使用 `warning / success / dead`。

建议：

- 统一只使用全局已有的状态语义，例如：
  - `pending`
  - `in-progress`
  - `done`
  - `cancelled`
  - `absent`
- 如果后续确实还需要“库存不足”“可预约”“已满”这类业务语义，也建议正式补到全局样式中，不要继续在页面里临时发明状态名。

验收标准：

- 页面里不再出现只在单页生效的状态语义。
- 预约、处方、库存、随访等页面的状态颜色语言一致。

### P1. `--border-light` 仍是悬空 token

当前多个业务页已经在使用 `--border-light`，但全局 token 文件里还没有正式定义它。

重点检查文件：

- `frontend/src/styles/design-tokens.css`
- `frontend/src/views/medical/prescription/PrescriptionPage.vue`
- `frontend/src/views/admin/drug/DrugStockPage.vue`
- `frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- `frontend/src/views/admin/dict/DictManagePage.vue`

建议二选一：

1. 在 `design-tokens.css` 中正式补上 `--border-light`
2. 或者把页面里的 `--border-light` 全部替换为已有 token，比如 `--border` 或 Element 的边框变量

我的建议：

- 如果已经有多个页面在用，直接正式补一个 `--border-light` 更稳，也更利于后面复用。

验收标准：

- 页面里不再依赖未定义 token。
- 分隔线和浅边框在不同页面表现一致。

### P2. 发药页文案语气还可以再收一轮

发药页的结构已经比较成熟，但局部文案还是稍微偏“演示感”，和现在整体已经建立起来的医疗政务风不完全一致。

重点检查文件：

- `frontend/src/views/medical/dispense/DispensePage.vue`

建议：

- 把提示语、成功提示、空状态文案再收得更专业一点。
- 尽量减少情绪化表达和过度口语化表达。
- 保留清晰度，但更接近正式医疗信息系统。

示例方向：

- `请先从左侧选择待发药处方` 比 `先挑一张待发药处方开始吧` 更合适
- `发药登记已完成` 比 `发药成功，流程已闭环` 更克制
- `当前暂无待处理记录` 比 `今天的发药任务已经清空` 更正式

验收标准：

- 整页语气和登录页、驾驶舱、居民首页一致。
- 文案更像正式系统，不像演示稿。

### P3. 页面内联样式仍可再减一轮

当前页面视觉已经很好，但还有一部分内联样式存在，说明设计系统已经建起来了，不过复用层还可以再往前推一点。

重点检查文件：

- `frontend/src/views/resident/appointment/AppointmentPage.vue`
- `frontend/src/views/medical/prescription/PrescriptionPage.vue`
- `frontend/src/views/medical/dispense/DispensePage.vue`

建议：

- 把重复出现的 margin、宽度、布局微调继续提到公共 class。
- 如果某个结构在两个页面以上都出现，就优先放到 `components.css`。
- 不需要为了“零内联样式”而过度抽象，但常见结构值得继续沉淀。

可以优先观察的结构：

- 摘要行
- 记录卡
- 工具栏按钮组
- 右侧信息栏
- 表单区域标题行

验收标准：

- 高频业务页内联样式明显减少。
- 公共样式文件继续承担更多布局职责。

## 3. 当前完成度判断

结合现在的整体状态，我建议这样定义前端完成度：

- 前端 UI 重构完成度：90% - 93%
- 设计系统成熟度：93% - 96%
- 核心业务页覆盖度：86% - 90%
- 可用于答辩/展示的完成度：95% 左右

一句话总结就是：

前端主体已经完成，现在剩下的是最后一轮语义统一、文案收口和样式工程化整理。

## 4. 是否必须继续改

如果目标是：

- 课程答辩
- 项目展示
- 中期或终期验收

那么当前状态已经足够支撑。

如果目标是：

- 组内长期维护
- 后续继续扩展更多业务页
- 希望形成更稳的前端规范

那么仍然建议把本清单中的 3 到 4 项问题收干净。

## 5. 建议执行顺序

建议按这个顺序完成最后收尾：

1. 统一预约页和处方页的状态语义
2. 补齐或替换 `--border-light`
3. 收紧发药页文案语气
4. 再做一次轻量去内联样式

## 6. 可直接交给其他 AI 的 Prompt

```text
请对 E:\ALL\gptcodex\NEW\frontend 做最后一轮轻量收尾优化，只处理语义统一、token 补齐、文案收口和少量去内联样式，不要大改结构。

本轮重点文件：
- frontend/src/views/resident/appointment/AppointmentPage.vue
- frontend/src/views/medical/prescription/PrescriptionPage.vue
- frontend/src/views/medical/dispense/DispensePage.vue
- frontend/src/styles/design-tokens.css
- frontend/src/styles/components.css

本轮目标：
1. 统一剩余页面的 status-tag 语义
2. 补齐或替换悬空的 --border-light token
3. 收紧发药页文案语气
4. 继续减少高频业务页中的内联样式

约束：
- 不改后端接口
- 不删业务逻辑
- 不重写页面结构
- 不新增装饰性动画
- 优先复用现有 design-tokens.css 和 components.css

输出要求：
- 先说明准备如何处理
- 再直接改代码
- 最后总结修改了哪些文件、统一了哪些语义、还有没有残余问题
```

## 7. 最终结论

这套前端现在已经不是“还需要大幅度改造”的状态，而是“可以收尾、可以稳定交付”的状态。

如果还有时间，继续改是为了：

- 更统一
- 更规范
- 更方便后续维护

如果现在停下，也已经足够支撑完整展示和答辩。
