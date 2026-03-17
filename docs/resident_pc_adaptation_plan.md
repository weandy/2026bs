# 居民端 PC 网页适配优化计划

## 1. 文档目的

这份文档用于说明当前居民端在 PC 网页端的适配情况，并给出一套可执行的优化计划。

当前项目的居民端设计与实现明显是**移动端优先**，这本身没有问题，也符合社区居民使用习惯。但如果后续答辩需要展示居民端 PC 页面，或者希望三类角色都具备较稳定的桌面端展示效果，那么居民端还需要补一轮专门的桌面适配。

---

## 2. 当前总体判断

### 2.1 现状结论

当前居民端的桌面端状态可以概括为：

**“可以显示，但不是专门为 PC 设计的页面。”**

更准确一点说：

- 移动端体验已经比较完整
- PC 端大多数页面没有炸
- 但 PC 端更多是“手机页面居中显示”或“移动结构放大”
- 还没有形成真正的桌面导航、桌面信息布局和桌面交互节奏

### 2.2 完成度评估

按居民端 PC 网页适配单独评估：

- 可用性：`70%-78%`
- 布局适配完成度：`55%-65%`
- 桌面端体验完成度：`45%-55%`
- 综合完成度：`58%-68%`

这意味着：

- 如果只是临时演示，当前版本可以用
- 如果希望居民端在答辩时也像医护端、管理端一样显得成熟，就需要继续优化

---

## 3. 当前主要问题

## 3.1 ResidentLayout 仍然是纯移动端壳层

文件：

- [frontend/src/components/layout/ResidentLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/ResidentLayout.vue)

当前问题：

- 底部 Tab 在所有端都固定显示
- 没有桌面端专用导航
- 内容区域永远给底部导航预留空间
- PC 上的整体使用习惯仍然像手机 H5

具体表现：

- 大屏上底部固定五栏导航不够自然
- 页面上方缺少桌面端统一入口
- 页面切换依然依赖底部导航，不像网页应用

这说明当前居民端的布局层没有做“移动端和 PC 端分流”。

---

## 3.2 首页在 PC 上过窄，信息利用率低

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

当前问题：

- `max-width: 500px`
- PC 上整体仍然是单列结构
- 两侧留白很多，但没有被利用
- Hero、提醒、快捷服务、预约、公告都还是纵向堆叠

具体后果：

- 视觉上像“手机页被放大”
- 看起来不够像桌面居民服务首页
- 首页在 PC 上的展示感明显弱于医护端和管理端

---

## 3.3 第二梯队页面缺少桌面端增强逻辑

重点页面：

- [frontend/src/views/resident/visit-record/VisitRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/visit-record/VisitRecordPage.vue)
- [frontend/src/views/resident/message/MessagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/message/MessagePage.vue)
- [frontend/src/views/resident/ProfilePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/ProfilePage.vue)
- [frontend/src/views/resident/vaccine/VaccinePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/vaccine/VaccinePage.vue)

当前问题：

- 大多数页面只有“移动端补丁”
- 缺少 `min-width` 或桌面断点下的增强布局
- 没有专门把 PC 端页面改成双栏、主次分区或详情面板

也就是说，目前代码主要是在做：

- 小屏修补

而不是：

- 大屏增强

---

## 3.4 消息和记录页仍偏移动端阅读模型

### 消息页

文件：

- [frontend/src/views/resident/message/MessagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/message/MessagePage.vue)

当前问题：

- PC 上仍是单列消息卡片
- 更适合升级成“左侧列表 + 右侧详情”的桌面消息中心

### 就诊记录页

文件：

- [frontend/src/views/resident/visit-record/VisitRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/visit-record/VisitRecordPage.vue)

当前问题：

- PC 上仍然通过弹窗查看详情
- 更适合桌面端的方式是“记录列表 + 详情区”

---

## 4. 哪些页面当前 PC 表现相对较好

虽然整体偏移动端，但也不是所有页面都不适合桌面。

### 4.1 预约挂号页

文件：

- [frontend/src/views/resident/appointment/AppointmentPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/appointment/AppointmentPage.vue)

优点：

- 已经有双列布局
- 摘要卡和表单区分明确
- 在居民端里是比较接近桌面结构的一页

问题：

- 历史预约区还可以进一步桌面化
- 部分按钮和摘要区仍是偏移动端的布局习惯

### 4.2 健康档案页

文件：

- [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue)

优点：

- 已经有三列信息区
- 大屏下信息密度更合理
- 是当前居民端桌面表现最好的一页之一

问题：

- 不需要大改，主要是进一步统一局部细节即可

---

## 5. 优化目标

这轮居民端 PC 适配不建议推翻移动端思路，而应该采用：

**“保留移动端为主、关键页面在桌面端增强展示”**

目标不是把居民端做成后台系统，而是让它在 PC 浏览器中：

- 结构更稳定
- 导航更自然
- 信息利用率更高
- 更适合答辩展示

---

## 6. 推荐优化策略

## 6.1 布局层分端处理

### ResidentLayout 调整方向

文件：

- [frontend/src/components/layout/ResidentLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/ResidentLayout.vue)

建议：

1. 移动端继续保留底部 Tab
2. PC 端隐藏底部 Tab
3. PC 端新增一个轻量顶部导航或页头导航

推荐样式：

- 左侧放平台标题或居民服务入口名
- 中间放核心导航：
  - 首页
  - 预约
  - 健康档案
  - 消息
  - 我的
- 右侧放用户名和退出入口

这样做的好处：

- 手机不受影响
- PC 上不再像 H5
- 不需要完全重做居民端结构

### 最小实现方案

如果时间紧，不必大改路由，只要：

- 在 `768px` 或 `992px` 以上隐藏底部导航
- 新增一个简单的桌面顶部导航条

就能明显提升展示效果。

---

## 6.2 首页做桌面双栏或三段布局

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

建议重组：

### 顶部区域

- 左：Hero
- 右：今日提醒

### 中间区域

- 整块：快捷服务

### 下方区域

- 左：我的预约
- 右：社区公告 / 消息通知

建议断点：

- `>= 1024px` 使用双栏或 `2 + 1` 结构
- `< 1024px` 保持当前单列

这样既保留移动端，又能让 PC 首页更像桌面服务门户。

---

## 6.3 消息页升级为桌面消息中心

文件：

- [frontend/src/views/resident/message/MessagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/message/MessagePage.vue)

建议：

### 移动端

- 保持当前卡片流

### PC 端

- 左侧：消息列表
- 右侧：消息详情

如果不想大改交互，也可以先做简化版：

- 左侧消息列表仍旧保留
- 点击后在右侧展示详情卡
- 不再完全依赖单列卡片阅读

这是最容易体现 PC 端差异化的一页。

---

## 6.4 就诊记录页升级为列表 + 详情布局

文件：

- [frontend/src/views/resident/visit-record/VisitRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/visit-record/VisitRecordPage.vue)

建议：

### 当前

- 卡片列表
- 点击后弹窗详情

### 建议 PC 端

- 左侧：记录列表
- 右侧：当前记录详情

这样有三个好处：

- 大屏利用率更高
- 比弹窗更适合桌面浏览
- 答辩演示时信息更完整、更稳定

移动端则可以继续保留当前弹窗模式。

---

## 6.5 个人中心做桌面化信息分区

文件：

- [frontend/src/views/resident/ProfilePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/ProfilePage.vue)

当前：

- 单列卡片

建议：

- PC 端做成左右分区
- 左侧放个人资料与紧急联系人
- 右侧放系统设置与安全操作

这样改动成本不高，但 PC 观感会明显提升。

---

## 6.6 疫苗页补充桌面栅格策略

文件：

- [frontend/src/views/resident/vaccine/VaccinePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/vaccine/VaccinePage.vue)

建议：

- 保持现有 tabs 结构
- PC 端下，疫苗卡片区保持两列甚至三列
- 预约记录和接种记录部分可以加大内容区域宽度
- 补更明显的表头和信息分区

---

## 7. 文件级执行任务单

## P0：必须先做

### 1. ResidentLayout.vue

文件：

- [frontend/src/components/layout/ResidentLayout.vue](/E:/ALL/gptcodex/NEW/frontend/src/components/layout/ResidentLayout.vue)

任务：

- 增加桌面端导航
- PC 隐藏底部 Tab
- 移动端保留当前底部 Tab

验收标准：

- 手机端不受影响
- PC 端出现明确一级导航
- 页面不再强依赖底部导航切换

### 2. HomePage.vue

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

任务：

- 增加桌面双栏布局
- 首页从单列改成分区展示
- 提高大屏信息利用率

验收标准：

- PC 首页不再是 500px 窄列居中
- Hero、提醒、预约、公告之间形成清楚分区

---

## P1：优先继续做

### 3. MessagePage.vue

文件：

- [frontend/src/views/resident/message/MessagePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/message/MessagePage.vue)

任务：

- 增加桌面消息中心结构
- 至少实现左列表右详情的基础版

### 4. VisitRecordPage.vue

文件：

- [frontend/src/views/resident/visit-record/VisitRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/visit-record/VisitRecordPage.vue)

任务：

- 增加桌面详情面板
- 减少仅靠弹窗承载详情的方式

### 5. ProfilePage.vue

文件：

- [frontend/src/views/resident/ProfilePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/ProfilePage.vue)

任务：

- 桌面端改成双区块或双栏
- 提高资料区与设置区的分层

---

## P2：有余力再补

### 6. VaccinePage.vue

文件：

- [frontend/src/views/resident/vaccine/VaccinePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/vaccine/VaccinePage.vue)

任务：

- 优化 PC 下卡片栅格和记录区布局

### 7. AppointmentPage.vue

文件：

- [frontend/src/views/resident/appointment/AppointmentPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/appointment/AppointmentPage.vue)

任务：

- 历史预约区进一步桌面化
- 让新建预约与历史预约在桌面端更均衡

### 8. HealthRecordPage.vue

文件：

- [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue)

任务：

- 只做局部细节优化
- 不建议大改

---

## 8. 答辩前最小可行方案

如果时间有限，不必把所有居民端页面都做成完整桌面版。

答辩前最小可行方案建议只完成这 4 项：

1. `ResidentLayout.vue` 增加 PC 导航
2. `HomePage.vue` 改成桌面双栏首页
3. `MessagePage.vue` 改成桌面消息中心基础版
4. `VisitRecordPage.vue` 改成列表 + 详情基础版

只要这 4 项完成，居民端在 PC 上的整体观感就会提升很多，足够支持演示。

---

## 9. 最终建议

居民端后续不需要追求和管理后台一样“强桌面化”，因为它本来就应该偏移动端。

最合理的方向是：

**保留移动端优先的产品定位，同时补足 PC 端首页、导航和两个关键信息页的桌面展示能力。**

这样既不会推翻现有成果，也能让答辩时三类角色的展示更完整、更平衡。
