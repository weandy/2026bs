# 前端 UI 复查文档（二轮）

本文档用于记录本轮前端 UI 改造复查结果，重点说明：

- 当前已经解决了哪些问题
- 还剩哪些设计与 UX 问题
- 哪些问题优先级更高
- 下一轮适合如何继续修改

适用范围：

- 项目根目录：`E:\ALL\gptcodex\NEW`
- 前端目录：`E:\ALL\gptcodex\NEW\frontend`

## 1. 本轮复查结论

和上一轮相比，这次前端已经有了明显进步，尤其体现在以下几点：

- 布局导航已经改为配置驱动
- 管理驾驶舱已经从旧统计页升级到新版结构
- 医护工作台已经去掉 emoji，并开始使用 schema 驱动体征录入
- 居民首页已经补上提醒和健康摘要
- 全局设计 token 和组件语义仍然保持良好

如果重新评估前端 UI 改造完成度，可以判断为：

- 基础设计系统：`80%-88%`
- 样板页完成度：`70%-80%`
- 全站统一改造完成度：`70%-78%`

当前阶段已经不是“要不要改”的问题，而是“如何把剩余不一致的部分继续收口”。

## 2. 本轮复查后仍存在的问题

### 2.1 P1：移动端导航仍存在可达性缺口

文件：

- [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)

现状：

- 桌面端侧边导航已经通过配置数组统一生成
- 但移动端仍然直接取 `currentNav` 的前 5 项

对应代码位置：

- [frontend/src/components/layout/AdminLayout.vue#L127](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue#L127)

问题说明：

- 医护端和管理端都有超过 5 个功能页
- 当前做法会让后面的页面在移动端没有入口
- 这属于典型的“桌面端修好了，移动端还没跟上”

建议修改：

- 不要直接 `slice(0, 5)`
- 改成：
  - 保留 4 个高频入口
  - 第 5 个入口改为“更多”
- 点击“更多”后打开抽屉、底部动作面板或弹出菜单
- 把剩余菜单项都放到“更多”里

推荐目标：

- 医护端移动端可访问：
  - 工作台
  - 处方
  - 档案
  - 随访
  - 更多
- 管理端移动端可访问：
  - 首页
  - 用户
  - 排班
  - 药品
  - 更多

### 2.2 P2：登录页仍然偏“展示型品牌页”

文件：

- [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)

现状：

- 已经把演示账号收进帮助区，这一步是对的
- 图标也统一到了 `lucide-vue-next`
- 但左侧品牌区依然保留了较重的渐变、模糊、装饰圆形和一定的“宣传页”气质

对应位置：

- [frontend/src/views/public/LoginPage.vue#L173](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue#L173)
- [frontend/src/views/public/LoginPage.vue#L206](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue#L206)
- [frontend/src/views/public/LoginPage.vue#L275](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue#L275)

问题说明：

- 当前页面已经好看很多，但还不完全符合“社区卫生服务中心平台”的定位
- 它更像一个展示型登录落地页，而不是政务医疗入口页

建议修改：

- 背景继续去装饰化
- 左侧品牌区减少模糊与发光感
- `feature-item` 去掉 `backdrop-filter`
- 左侧不要再强调“品牌感”，改为更稳的机构说明区
- 标题和副标题继续保留，但降低装饰性

推荐方向：

- 保留双栏布局
- 保留当前登录逻辑
- 继续减少“营销页感”

### 2.3 P2：居民首页已经更完整，但与全局组件体系的融合还不够彻底

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

现状：

- 提醒、快捷服务、健康摘要、预约、消息都已经有了
- 页面信息结构比之前完整很多

对应位置：

- [frontend/src/views/resident/HomePage.vue#L16](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue#L16)
- [frontend/src/views/resident/HomePage.vue#L48](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue#L48)
- [frontend/src/views/resident/HomePage.vue#L233](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue#L233)

问题说明：

- 页面里仍有一些较重的私有视觉效果
- Hero 区的呼吸动画和装饰圆形会让居民首页显得比其他页更“活跃”
- 这和整体医疗政务风的克制感略有偏差

建议修改：

- Hero 区可以保留层次感，但弱化动画
- 如果不是答辩展示页，建议移除持续动画
- 提醒卡、摘要卡、列表卡尽量更多复用全局类
- 快捷服务项的背景色建议由全局 token 管理，而不是页面内继续手写 `rgba`

推荐目标：

- 居民首页更安静、更清楚
- 不靠动画吸引注意力，而靠摘要和结构引导阅读

### 2.4 P3：样板页还有少量页面内私有样式，说明系统化还没完全吃透

文件：

- [frontend/src/views/admin/dashboard/DashboardPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dashboard/DashboardPage.vue)
- [frontend/src/views/medical/workbench/WorkbenchPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/workbench/WorkbenchPage.vue)
- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

问题说明：

- 现在已经有 `metric-card-v2`、`alert-bar`、`panel`、`status-tag` 这些全局组件语义
- 但页面里仍保留了一些局部 class 和少量内联写法
- 这不会立刻造成 bug，但会在后面全站推进时增加维护成本

建议修改：

- 继续把以下能力提到 `components.css`：
  - 页面头部 `page-header`
  - 卡片列表项 `list-row`
  - 摘要区 `summary-grid`
  - 快速操作区 `action-stack`
- 页面文件只保留与当前业务强相关的样式
- 通用结构尽量从页面中抽走

## 3. 当前做得比较好的部分

### 3.1 导航配置方式已经正确

文件：

- [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)

优点：

- 菜单已经不再手写两套
- 医护和管理导航都改成配置数组
- 比之前更适合长期维护

### 3.2 管理驾驶舱已经成为新版样板页

文件：

- [frontend/src/views/admin/dashboard/DashboardPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/admin/dashboard/DashboardPage.vue)

优点：

- 已经有新版页面头部
- 已经加入预警条
- 指标卡已经用上 `metric-card-v2`
- 公告、图表、预警摘要比之前层次清楚很多

### 3.3 医护工作台的结构已经比较成熟

文件：

- [frontend/src/views/medical/workbench/WorkbenchPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/medical/workbench/WorkbenchPage.vue)

优点：

- 三栏结构清晰
- 状态标签、风险条、慢病标签都已经统一
- 体征输入已从硬编码切成 schema 驱动

### 3.4 居民首页方向基本对了

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

优点：

- 长者模式保留得很好
- 增加了提醒区和健康摘要区
- 图标语言基本统一

## 4. 下一轮建议优先顺序

建议下一轮继续按这个顺序推进：

1. 修移动端导航“更多”入口
2. 收敛登录页的装饰感
3. 弱化居民首页动画和私有视觉效果
4. 把样板页中可复用结构继续提升到全局组件层
5. 开始向预约、档案、处方、库存、字典等业务页复制新版风格

## 5. 可直接交给其他 AI 的增量任务 Prompt

下面这段可以直接复制给其他 AI：

```text
请继续推进 E:\ALL\gptcodex\NEW\frontend 的 UI 渐进式重构。

参考资料：
- E:\ALL\gptcodex\NEW\docs\frontend_ui_refactor_handoff.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_review_round2.md
- E:\ALL\gptcodex\NEW\ui-prototype

要求：
- 不改后端接口
- 不删除业务逻辑
- 保持当前设计系统方向
- 优先复用：
  - frontend/src/styles/design-tokens.css
  - frontend/src/styles/components.css
  - frontend/src/styles/global.css
- 尽量减少页面私有色值、私有动画和内联样式
- 风格保持医疗政务风：可信、克制、清爽、专业

本轮优先处理：

1. frontend/src/components/layout/AdminLayout.vue
- 不要再用 currentNav.slice(0, 5) 作为移动端导航策略
- 改成“4 个主入口 + 更多”
- 通过抽屉、弹层或菜单承载剩余导航项

2. frontend/src/views/public/LoginPage.vue
- 保留双栏布局与现有登录逻辑
- 继续减少背景装饰、模糊、营销感
- 让页面更像社区卫生服务中心统一入口

3. frontend/src/views/resident/HomePage.vue
- 减少 hero 区动画感
- 让页面进一步贴近全局组件体系
- 尽量把私有结构提升为可复用样式

4. 继续提炼通用结构到 frontend/src/styles/components.css
- page-header
- summary-grid
- list-row
- action-stack

输出要求：
- 先说明你发现的问题
- 再直接修改代码
- 最后总结：
  - 修改了哪些文件
  - 哪些地方做了抽象复用
  - 还有哪些后续建议
```

## 6. 一句话总结

当前前端已经进入“样板页成熟，开始做全站收口”的阶段。接下来最重要的不是大改方向，而是继续把移动端可达性、视觉克制感和组件复用度补齐。
