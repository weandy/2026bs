# 前端 UI 第三轮可执行任务单

本文档用于把下一轮前端 UI 改造任务拆成可执行清单，便于直接分发给其他 AI 或前端同学。

目标：

- 不推翻当前设计系统
- 在现有改造成果上继续收口
- 解决剩余的可达性、一致性和视觉克制问题

适用范围：

- 项目路径：`E:\ALL\gptcodex\NEW`
- 前端路径：`E:\ALL\gptcodex\NEW\frontend`

## 1. 本轮总目标

本轮只做下面四件事：

1. 修复移动端导航可达性
2. 收敛登录页视觉风格
3. 收敛居民首页视觉风格
4. 继续把通用结构提升到全局组件样式层

## 2. 执行顺序

建议严格按这个顺序执行：

1. [frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)
2. [frontend/src/styles/components.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/components.css)
3. [frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)
4. [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

原因：

- 先修导航，可避免后续页面虽存在但无法进入
- 先补全局样式，再收页面，返工最少

## 3. 文件级任务清单

### 任务 A：[frontend/src/components/layout/AdminLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/AdminLayout.vue)

#### 目标

- 解决移动端导航入口不足的问题
- 保持当前配置驱动结构不变

#### 现在的问题

- `mobileNav` 直接对 `currentNav` 做了 `slice(0, 5)`
- 超过 5 个的功能在移动端无入口

#### 要删什么

- 删除“直接截前 5 个”的策略

#### 要加什么

- 新增“更多”入口
- 新增一个移动端菜单面板
  - 推荐用 `el-drawer`
  - 或 `el-dialog` + 列表
- 把未显示在主导航中的项全部放到“更多”里

#### 推荐实现方式

- 保留 `currentNav`
- 新增：
  - `primaryMobileNav`
  - `overflowMobileNav`
  - `showMoreNav`
- 主导航显示前 4 个高频项
- 第 5 个用固定“更多”

#### 验收标准

- 医护端所有页面在移动端都能进入
- 管理端所有页面在移动端都能进入
- 桌面端导航不受影响
- 路由结构不需要修改

#### 风险提醒

- 不要把“更多”入口也写死成一堆重复模板
- 仍然建议复用同一份 `currentNav`

### 任务 B：[frontend/src/styles/components.css](/E:/ALL/gptcodex/NEW/frontend/src/styles/components.css)

#### 目标

- 提升全局复用度
- 减少页面继续写私有结构样式

#### 要加什么

建议补这几组通用 class：

- `.page-header`
  - 页面标题 + 副标题 + 操作区
- `.summary-grid`
  - 摘要卡网格
- `.list-row`
  - 通用列表项结构
- `.action-stack`
  - 纵向快捷操作区
- `.more-link`
  - 统一“查看更多”链接

#### 推荐实现方式

- 样式保持轻量
- 命名延续当前 `panel`、`metric-card-v2` 的风格
- 不要把业务语义写死在 class 里

#### 验收标准

- LoginPage 和 HomePage 至少能复用其中一部分新样式
- 后续 Dashboard/Workbench 也可继续复用

### 任务 C：[frontend/src/views/public/LoginPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/public/LoginPage.vue)

#### 目标

- 让登录页从“展示型品牌页”进一步收敛为“机构入口页”

#### 现在的问题

- 左侧渐变仍然比较重
- 装饰圆形和模糊效果还在
- 整体仍有较强展示感

#### 要删什么

- 删弱过重的装饰圆形效果
- 删弱 `filter: blur(...)`
- 删弱 `backdrop-filter`
- 删弱太强的阴影和光感

#### 要保留什么

- 双栏布局
- 当前登录逻辑
- 帮助区折叠方式
- 当前图标方案

#### 要加什么

- 更清晰的机构说明文案层级
- 更稳的说明区留白
- 更统一的输入框与按钮节奏

#### 建议改法

- 左侧保留主色，但降低渐变反差
- `feature-item` 从“漂浮标签”改成更朴素的说明条
- 页面背景减少两侧氛围装饰
- 标题、副标题和按钮更贴近平台入口页语气

#### 验收标准

- 页面仍然有识别度
- 但不再有明显“营销登录页”感
- 居民、医护、管理都适合从这个页面进入

### 任务 D：[frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

#### 目标

- 让居民首页更克制、更统一
- 减少页面级特殊效果

#### 现在的问题

- Hero 区仍然有呼吸动画和装饰圆形
- 快捷服务背景色仍有页面级手写值
- 页面在视觉上比其他页面更“活跃”

#### 要删什么

- 删掉或显著减弱 `hero-breathe`
- 删掉不必要的装饰圆形伪元素
- 删弱页面级“展示感”写法

#### 要保留什么

- 今日提醒
- 健康摘要
- 长者模式开关
- 快捷服务结构
- 预约和消息数据获取逻辑

#### 要加什么

- 更多对全局通用 class 的复用
- 如果需要图标背景色，优先走 token 或统一 helper class

#### 建议改法

- Hero 改成静态层次，不依赖动画
- 提醒卡、摘要卡、列表卡尽量复用统一卡片节奏
- 让首页视觉和 ResidentLayout、健康档案页更接近

#### 验收标准

- 首页仍然有层次感
- 但比现在更克制、更安静
- 和其他页面放在一起时不会显得风格跳脱

## 4. 不要做的事情

本轮明确不要做这些：

- 不要改后端接口
- 不要重写路由结构
- 不要把全局 token 推翻重来
- 不要新增一套独立的第二设计系统
- 不要大规模引入新的 UI 库
- 不要把简单页面重构成过于复杂的组件树

## 5. 本轮完成后的验收方式

完成后至少检查以下内容：

### 导航

- 桌面端侧边栏功能是否完整
- 医护端移动端是否能进入接种管理和我的排班
- 管理端移动端是否能进入科室、疫苗、字典、日志、调班审批

### 登录页

- 登录逻辑是否保持正常
- 页面视觉是否更克制
- 帮助区是否仍可展开

### 居民首页

- 长者模式是否仍正常工作
- 今日提醒、健康摘要、消息、预约是否都正常显示
- 页面是否去掉了多余动画感

### 全局样式

- 新增 class 是否真的被页面复用
- 是否减少了页面内样式重复

## 6. 可直接给其他 AI 的任务 Prompt

```text
请继续处理 E:\ALL\gptcodex\NEW\frontend 的前端 UI 收口任务。

参考资料：
- E:\ALL\gptcodex\NEW\docs\frontend_ui_refactor_handoff.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_review_round2.md
- E:\ALL\gptcodex\NEW\docs\frontend_ui_tasklist_round3.md

本轮目标：
1. 修复移动端导航可达性
2. 收敛登录页视觉风格
3. 收敛居民首页视觉风格
4. 继续提炼全局可复用样式

只处理以下文件：
- frontend/src/components/layout/AdminLayout.vue
- frontend/src/styles/components.css
- frontend/src/views/public/LoginPage.vue
- frontend/src/views/resident/HomePage.vue

约束：
- 不改后端接口
- 不删业务逻辑
- 不推翻现有 token 系统
- 尽量减少内联样式和页面私有写法
- 风格保持医疗政务风：可信、克制、专业、清爽

具体要求：

1. AdminLayout.vue
- 不要再直接 slice 前 5 个导航项
- 改成“4 个主入口 + 更多”
- 通过抽屉、菜单或弹层承载剩余导航项

2. components.css
- 新增 page-header、summary-grid、list-row、action-stack、more-link 等可复用样式

3. LoginPage.vue
- 保留双栏和登录逻辑
- 去掉过强渐变、模糊、装饰感
- 更接近机构统一入口

4. HomePage.vue
- 弱化或移除 hero 动画
- 更多复用全局样式
- 保留提醒、摘要、长者模式和现有数据逻辑

输出要求：
- 先说明你准备如何改
- 再直接修改代码
- 最后汇总：
  - 修改了哪些文件
  - 删除了哪些冗余视觉效果
  - 新增了哪些全局可复用样式
  - 是否还有下一轮建议
```

## 7. 一句话总结

第三轮的重点不是继续“做新页面”，而是把已经改好的方向继续收口，让导航、登录页、居民首页和全局样式真正统一起来。
