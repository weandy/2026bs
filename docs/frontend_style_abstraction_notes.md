# 前端样式抽象说明文档

本文档写给项目组内继续参与前端改造的同学，主要说明：

- `info-block`、`record-card` 这类名字是什么意思
- 它们是不是项目里已经存在的固定类名
- 当前哪些结构建议现在抽成公共样式
- 哪些结构先不要过度抽象

适用范围：

- 项目路径：`E:\ALL\gptcodex\NEW`
- 前端路径：`E:\ALL\gptcodex\NEW\frontend`

## 1. 先说结论

`info-block` 和 `record-card` 不是项目里已经存在的固定标准类名。

它们只是前端重构过程中，为了讨论“哪些结构值得抽象成公共样式”而提出的**建议命名**。

也就是说：

- 不是必须马上在代码里加这两个类
- 不是看到这个名字就说明项目里已经统一过
- 它们更像“未来可能值得抽的结构类型”

## 2. 为什么会提到这种名字

在这轮前端改造里，我们已经开始从“单页面写样式”往“全局设计系统 + 公共结构复用”推进。

目前已经有一批公共样式沉淀下来了，比如：

- `status-tag`
- `chronic-tag`
- `metric-card-v2`
- `alert-bar`
- `panel`
- `stepbar`
- `page-header`
- `summary-grid`
- `list-row`
- `action-stack`
- `more-link`

这些都是**已经比较明确、复用价值很高**的结构。

而 `info-block`、`record-card` 这类名字，是在进一步观察页面时发现：

- 有一些结构在多个页面里“可能会重复出现”
- 但还没有到必须马上抽成公共类的程度

所以才会用一个通用名字先描述它们。

## 3. `info-block` 到底指什么

`info-block` 指的是一种比较通用的信息块结构，通常长这样：

- 上面一行是标题
- 下面是一段说明文字或描述内容
- 适合展示病史、说明、备注、提示性文本

例如当前健康档案页里这类结构就很接近：

- [HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue#L85)
- [HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue#L89)

你们现在页面里写的是：

- `history-block`
- `history-label`
- `history-text`

这是完全合理的。

只有在未来这些结构在多个页面重复出现时，才值得考虑统一抽象成更通用的：

- `info-block`
- `info-block-title`
- `info-block-text`

### 当前建议

现在先**不要为了抽象而抽象**。

也就是说：

- 目前保留 `history-block` 这种页面内语义命名就可以
- 只有当就诊记录页、随访页、公卫页也出现类似结构时，再考虑统一

## 4. `record-card` 到底指什么

`record-card` 指的是一种“记录型卡片”结构，通常长这样：

- 有标题
- 有日期或时间
- 有状态或摘要
- 整体是一个浅色卡片或列表项

例如当前健康档案页的近期就诊记录项就属于这一类：

- [HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue#L100)
- [HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue#L294)

当前页面内写法是：

- `visit-item`
- `visit-head`
- `visit-date`
- `visit-diag`
- `visit-comp`

这同样没有问题。

只有当下面这些页面都开始出现相似结构时，才值得考虑抽成公共类：

- 就诊记录页
- 随访记录页
- 接种记录页
- 公告列表页
- 审计日志摘要卡

### 当前建议

当前阶段，`record-card` 也**不是必须马上落地的类名**。

先保留业务语义命名更清楚：

- 就诊记录用 `visit-item`
- 预约记录用 `history-item`
- 公告记录用 `notice-item`

等同类卡片在多个页面重复出现，再做抽象会更稳。

## 5. 当前真正建议“现在就抽”的是什么

相比 `info-block` 和 `record-card`，下面这些结构更值得现在就抽成公共样式，因为复用概率高、收益更直接。

### 5.1 `summary-kv`

适合场景：

- 预约摘要
- 订单摘要
- 基础信息确认
- 成功回执

典型结构：

- 左边 label
- 右边 value

当前很适合抽，因为预约页已经反复出现了。

### 5.2 `receipt-row`

适合场景：

- 成功页回执
- 摘要卡
- 结果确认卡

如果多个“提交成功页”都会有这种格式，就很值得抽。

### 5.3 `stat-line`

适合场景：

- 健康档案里的生命体征
- 统计指标行
- 某类数据的“名称 + 数值 + 单位 + 徽章”结构

这个已经具备较强通用性。

### 5.4 `page-header`

这个已经抽了，而且方向是正确的。

适合继续推广到：

- 预约页
- 健康档案页
- 处方页
- 发药页
- 库存页

### 5.5 `list-row`

这个也已经抽了，后面可以继续往更多列表页推。

## 6. 怎么判断一个结构该不该抽

建议统一按下面规则判断：

### 可以抽的情况

满足任意一条就可以考虑抽：

- 同一种结构已经在 2 个以上页面重复出现
- 这个结构明显不是业务独有，而是通用 UI 结构
- 抽出来后能减少多个页面重复写样式
- 抽出来不会让类名丢失语义

### 先别抽的情况

满足下面任意一条，建议先不要抽：

- 这个结构只在当前页面出现一次
- 结构很业务化，抽出去反而会让命名变空泛
- 只是为了“看起来更高级”而抽象
- 抽完以后别人反而更看不懂页面在表达什么

## 7. 推荐命名原则

推荐遵循这条原则：

### 先业务语义，后通用抽象

也就是说：

- 当前只在健康档案页里用，就写 `history-block`
- 当前只在就诊记录里用，就写 `visit-item`
- 等多个页面重复出现，再升级成：
  - `info-block`
  - `record-card`
  - `summary-kv`
  - `stat-line`

这样更符合渐进式重构，而不是一开始就过度设计。

## 8. 当前项目里的实际建议

### 现在建议保留页面私有命名的

- `history-block`
- `history-label`
- `history-text`
- `visit-item`
- `visit-head`
- `visit-diag`
- `visit-comp`

原因：

- 这些结构现在还比较偏业务语义
- 直接看名字就知道它属于哪种内容

### 现在建议逐步抽到公共层的

- `summary-kv`
- `receipt-row`
- `stat-line`
- `page-header`
- `list-row`
- `action-stack`

原因：

- 这些已经明显是“结构型 UI”
- 后续跨页面复用概率高

## 9. 给后续同学的建议

后面继续改页面时，请优先遵守这几条：

1. 不要为了抽象而抽象
2. 不要看到一个重复结构就立刻起一个“大而全”的公共类名
3. 先确认它是否真的会跨页面复用
4. 优先抽“结构型 UI”，不要优先抽“业务内容型 UI”
5. 公共层越清楚越好，不要把公共样式变成另一个难维护系统

## 10. 一句话总结

`info-block` 和 `record-card` 不是当前项目里必须立即实现的类名，它们只是对“未来可能值得抽象的结构”的描述。  

当前真正值得马上抽的是：

- `summary-kv`
- `receipt-row`
- `stat-line`
- `page-header`
- `list-row`

而像 `history-block`、`visit-item` 这种结构，当前继续保留页面内语义命名是更稳妥的做法。
