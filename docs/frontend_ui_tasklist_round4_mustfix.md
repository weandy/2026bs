# 前端 UI 第四轮必须完成清单

本文档用于承接第三轮复查后的剩余问题。和前几轮不同，这一轮不再追求“继续优化一点”，而是明确哪些点必须完成，否则前端改造就还不能算真正收口。

适用范围：

- 项目路径：`E:\ALL\gptcodex\NEW`
- 前端路径：`E:\ALL\gptcodex\NEW\frontend`

## 1. 本轮定位

第四轮是“收口轮”，不是“发挥轮”。

这轮只做 4 件事：

1. 修掉移动端导航不可达问题
2. 继续收敛登录页的展示感
3. 继续收敛居民首页的动画和装饰感
4. 把重复出现的页面结构真正提升到全局样式层

本轮不建议再扩新的业务页面。

## 2. 本轮必须完成项

### 必须项 1：移动端导航不能再用 `slice(0, 5)`

文件：

- [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)

当前问题：

- 移动端导航仍然通过 `currentNav.filter(...).slice(0, 5)` 取前 5 项
- 这意味着医护端和管理端都有部分页面在移动端无入口

对应位置：

- [frontend/src/components/layout/AdminLayout.vue#L50](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue#L50)
- [frontend/src/components/layout/AdminLayout.vue#L127](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue#L127)

必须完成的改法：

- 不允许继续使用简单截断
- 改成“4 个主入口 + 更多”
- “更多”必须能打开剩余导航项
- 剩余导航项必须来源于同一份 `currentNav`

推荐实现：

- `primaryMobileNav = visibleNav.slice(0, 4)`
- `overflowMobileNav = visibleNav.slice(4)`
- 固定渲染一个“更多”按钮
- 点击“更多”后用以下任一方式承载：
  - `el-drawer`
  - `el-dialog`
  - `el-popover`

验收标准：

- 医护端移动端可进入：
  - 工作台
  - 处方
  - 档案
  - 随访
  - 接种管理
  - 我的排班
  - 发药（若角色可见）
- 管理端移动端可进入全部管理页面
- 不能因为角色判断导致“更多”菜单项丢失

### 必须项 2：登录页继续去展示化

文件：

- [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)

当前问题：

- 演示账号位置处理对了
- 但视觉风格仍偏品牌页和展示页

仍然需要处理的部分：

- 左侧大面积渐变反差偏强
- `feature-item` 仍然有玻璃感
- 装饰圆形和模糊还在
- 页面整体仍带一点“宣传页气质”

必须完成的改法：

- 去掉 `feature-item` 的 `backdrop-filter`
- 去掉或显著减弱 `.deco-circle`
- 降低左侧背景的渐变对比度
- 去掉不必要的 `text-shadow`
- 表单区保持现在的结构，不要重新发明交互

可以保留的部分：

- 双栏布局
- 当前图标
- 当前账号识别逻辑
- 当前帮助区

验收标准：

- 登录页看起来像“机构入口页”
- 而不是“宣传感较强的品牌落地页”
- 页面仍有层次，但更稳、更克制

### 必须项 3：居民首页去掉多余动画和装饰

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

当前问题：

- 这一轮虽然内容更丰富了，但 Hero 区和快捷区又加回了动画
- 方向偏离了“收口”，更像“继续做效果”

必须删弱的部分：

- `hero-breathe`
- Hero 的装饰圆形伪元素
- `shortcut-item` 的 stagger 动画

必须保留的部分：

- 今日提醒
- 健康摘要
- 快捷服务
- 我的预约
- 消息通知
- 长者模式

推荐处理方式：

- Hero 保留颜色和层次，但改成静态
- 快捷服务保留 hover，但不再有入场动画
- 页面整体优先突出信息，不靠动画吸引注意力

验收标准：

- 首页仍然有设计感
- 但不再是全站里最“活跃”的页面
- 和其他页面放一起时，风格更加一致

### 必须项 4：全局复用层必须补起来

文件：

- [frontend/src/styles/components.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/components.css)

当前问题：

- 之前计划里要补的通用结构类并没有真正加进去
- 导致页面仍然有不少私有样式

本轮至少要补的内容：

- `.page-header`
- `.page-header-main`
- `.page-header-actions`
- `.summary-grid`
- `.list-row`
- `.action-stack`
- `.more-link`

这些类的目标：

- 让 Dashboard、Workbench、HomePage、后续预约和库存页能复用
- 减少页面里重复写样式

验收标准：

- 至少 [LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue) 或 [HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue) 其中一个页面开始复用这些 class
- 不是只把 class 写进 `components.css` 但没人用

## 3. 本轮不要做的事情

第四轮请明确不要做下面这些事：

- 不要再扩新页面
- 不要继续给居民首页加动画
- 不要再给登录页加视觉特效
- 不要改后端接口
- 不要改路由结构
- 不要新引入设计风格分支
- 不要重新命名现有大量 class

## 4. 本轮执行顺序

按这个顺序做：

1. `AdminLayout.vue`
2. `components.css`
3. `LoginPage.vue`
4. `HomePage.vue`

原因：

- 先修导航可达性
- 再补全局复用层
- 最后收页面视觉

## 5. 本轮完成后的检查单

### 导航检查

- 医护端移动端能否进入所有需要的页面
- 管理端移动端能否进入所有需要的页面
- “更多”入口是否真能显示剩余项
- 不同角色下导航是否正常显示

### 登录页检查

- 是否明显去掉了玻璃感和装饰圆形
- 是否减少了营销感
- 表单功能是否仍正常

### 居民首页检查

- 是否删除了 Hero 呼吸动画
- 是否删除了快捷区 stagger 动画
- 首页结构是否仍完整

### 全局样式检查

- 新增 class 是否真的在页面里用了
- 页面私有样式是否减少

## 6. 可直接交给其他 AI 的严格 Prompt

```text
请处理 E:\ALL\gptcodex\NEW\frontend 的第四轮 UI 收口任务。

参考资料：
- E:\ALL\gptcodex\NEW\docs\frontend_ui_refactor_handoff.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_review_round2.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_tasklist_round3.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_tasklist_round4_mustfix.md

这轮是“必须完成”的收口轮，不是自由优化轮。

只允许修改以下文件：
- frontend/src/components/layout/AdminLayout.vue
- frontend/src/styles/components.css
- frontend/src/views/public/LoginPage.vue
- frontend/src/views/resident/HomePage.vue

必须完成的任务：

1. AdminLayout.vue
- 不允许再使用 currentNav.slice(0, 5) 作为移动端导航策略
- 必须实现“4 个主入口 + 更多”
- 更多中必须能访问剩余导航项

2. components.css
- 必须补充 page-header、summary-grid、list-row、action-stack、more-link 等通用结构类
- 新增的 class 不能只是定义，至少要有页面开始使用

3. LoginPage.vue
- 必须继续去展示化
- 去掉 feature-item 的 backdrop-filter
- 去掉或明显减弱 deco-circle
- 降低左侧渐变和 text-shadow 的存在感

4. HomePage.vue
- 必须去掉 hero-breathe 和快捷区 stagger 动画
- 保留当前页面结构和数据逻辑
- 让页面更克制、更稳

约束：
- 不改后端接口
- 不改路由结构
- 不新增其他页面改造
- 不要继续加动画
- 不要继续做“更炫”的效果

输出要求：
- 先说明你将如何完成这 4 项必须任务
- 再直接修改代码
- 最后总结：
  - 哪些必须项已经完成
  - 哪些文件做了复用抽象
  - 是否还有未完成项
```

## 7. 一句话总结

第四轮的核心不是“继续优化一点”，而是把剩下最关键的几个收口问题真正做完，让前端改造进入可稳定扩展的状态。
