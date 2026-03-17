# 居民首页接口与展示调整说明

## 1. 背景

当前居民端首页在登录后会额外发起两个请求：

- `GET /api/resident/health-record/summary`
- `GET /api/admin/sys/notice?page=1&size=3`

这两个请求都会在居民身份下带来问题：

1. `resident/health-record/summary` 在当前后端中并不存在。
2. `admin/sys/notice` 路径本身不对，而且就算改成真实的 `/admin/notice`，居民角色也不应该直接访问 `/admin/**`。

因此，这不是“居民登录异常”，而是**居民首页请求设计不合理**。

相关代码位置：

- 前端首页请求： [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue:203)
- 居民健康档案控制器： [backend/chp-resident/src/main/java/com/chp/resident/controller/HealthRecordController.java](/E:/ALL/gptcodex/NEW/backend/chp-resident/src/main/java/com/chp/resident/controller/HealthRecordController.java:12)
- 公告管理控制器： [backend/chp-admin/src/main/java/com/chp/admin/controller/SysController.java](/E:/ALL/gptcodex/NEW/backend/chp-admin/src/main/java/com/chp/admin/controller/SysController.java:23)
- 安全配置： [backend/chp-security/src/main/java/com/chp/security/config/SecurityConfig.java](/E:/ALL/gptcodex/NEW/backend/chp-security/src/main/java/com/chp/security/config/SecurityConfig.java:45)

---

## 2. 这次建议的调整方向

本轮建议分成三件事处理：

1. 直接取消首页对不存在的健康摘要接口调用。
2. 为居民首页新增一个**公共公告接口**，不要再复用管理员路径。
3. 把首页 Hero 区真正用起来，让副标题承载更有价值的提醒信息，同时把顶部问候和敬老版按钮做大一些。

---

## 3. 接口调整建议

### 3.1 取消健康摘要接口调用

当前代码：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue:214)

当前逻辑：

- 首页挂载后请求 `/resident/health-record/summary`
- 请求失败后静默

问题：

- 这个接口在后端不存在。
- 首页已经有“我的预约”“消息通知”“社区公告”等信息块，当前阶段不需要为了首页摘要再额外补一个临时接口。

建议：

- **直接删除**首页中的 `/resident/health-record/summary` 请求逻辑。
- 同时移除首页中依赖 `healthSummary` 的展示块，或者把这一块延后到后续版本再做。

更稳妥的做法是：

- 首页不承担健康摘要明细展示，只保留功能导航、提醒、预约、公告。
- 健康档案详情继续由 [frontend/src/views/resident/health-record/HealthRecordPage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/health-record/HealthRecordPage.vue) 承担。

### 3.2 新增公共公告接口

当前错误调用：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue:220)

当前问题：

- 请求路径写成了 `/admin/sys/notice`
- 真实控制器路径是 `/admin/notice`
- 但 `/admin/**` 按安全规则只允许管理员访问

建议方案：

新增一个公共公告接口，例如：

- `GET /public/notice?page=1&size=3`

不建议用 `/resident/notice`，原因是：

- 公告本身不是居民专属数据
- 以后候诊公屏、未登录展示页、居民首页都可能复用
- `/public/**` 已经在安全配置里开放，现有体系更顺

推荐实现方式：

1. 新增一个面向公共访问的公告查询控制器。
2. 只返回“已发布 + 生效中”的公告。
3. 默认按 `isTop`、`publishedAt` 排序。
4. 前端首页改为请求 `/public/notice`。

后端返回字段建议至少保留：

- `id`
- `title`
- `content`
- `isTop`
- `publishedAt`

因为当前首页只用到了这些字段。

---

## 4. 首页展示调整建议

### 4.1 Hero 区信息要更有用

当前 Hero 副标题位置：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue:151)

当前文案：

- `预约挂号 · 健康档案 · 慢病随访 · 公共卫生`

这个写法有两个问题：

1. 更像宣传语，不像真实首页信息。
2. 没有利用首页已经拿到的动态数据。

建议改成“动态摘要 + 固定补充”的思路。

优先级建议：

1. 如果有未读消息：显示未读消息数
2. 如果有待就诊预约：显示待就诊预约数
3. 如果有公告：显示最新公告数量
4. 没有动态数据时，再退回静态说明

推荐文案示例：

- `您有 2 个待就诊预约，1 条未读消息`
- `您有 3 条社区公告待查看`
- `预约挂号、健康档案和就诊记录可在首页快捷进入`

建议不要再写成纯功能串联口号。

### 4.2 首页保留哪些模块更合适

当前首页已经有这些块：

- 今日提醒
- 快捷服务
- 我的预约
- 消息通知
- 社区公告

这套结构其实已经够用了。

建议：

- 保留 `今日提醒`
- 保留 `快捷服务`
- 保留 `我的预约`
- 保留 `社区公告`
- `消息通知` 可以继续保留，但如果页面过长，也可以合并到“今日提醒”里，只保留“查看全部”入口

如果删除健康摘要块，首页会更聚焦，也更适合做“居民服务导航页”。

---

## 5. Hero 顶部尺寸调整建议

目标位置：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue:6)

当前问题：

- `晚上好，张建国` 的徽标偏小
- `当前：敬老版 · 切换` 按钮也偏小
- 在移动端尤其会显得顶部不够稳，不够像一级信息区

建议调整方向：

### 5.1 顶部问候徽标

当前：

- `font-size: 12px`
- `padding: 4px 12px`

建议：

- `font-size` 调到 `14px` 或 `15px`
- `padding` 调到 `8px 14px`
- `font-weight` 适当提高
- 行高增加，让它更像明确的身份提示

### 5.2 敬老版切换按钮

当前：

- `font-size: 11px`
- `padding: 4px 12px`

建议：

- `font-size` 调到 `13px` 或 `14px`
- `padding` 调到 `8px 14px`
- 最小点击高度至少保持在 `36px` 以上
- 按钮文案可以简化成：
  - `敬老版 · 已开启`
  - `切换标准版`

不要把“当前：敬老版 · 切换”全部挤在很小的按钮里。

### 5.3 Hero 区整体高度

当前 Hero 已经有不错的视觉基础，但顶部信息偏小，导致层级不够明显。

建议：

- 适当增加顶部 row 的间距
- 放大 `hero-badge` 和 `elder-toggle-btn`
- 保持标题与副标题层级，不要再增加额外装饰

调整重点是“可读性和点击性”，不是加动画。

---

## 6. 推荐执行方案

### 前端

文件：

- [frontend/src/views/resident/HomePage.vue](/E:/ALL/gptcodex/NEW/frontend/src/views/resident/HomePage.vue)

建议操作：

1. 删除 `/resident/health-record/summary` 请求逻辑
2. 删除或暂时隐藏首页健康摘要卡片
3. 把 `/admin/sys/notice` 改为未来的 `/public/notice`
4. 优化 Hero 副标题逻辑，改为动态摘要文案
5. 放大顶部问候和敬老版按钮

### 后端

建议新增：

- `GET /public/notice`

推荐放置位置：

- 新建公共公告控制器
- 或在已有公开控制器体系中增加一个面向居民首页的公告查询接口

建议不要直接把管理员控制器开放给居民用。

---

## 7. 验收标准

调整完成后，居民登录首页应满足：

1. 不再出现因为首页初始化请求导致的报错提示
2. 首页不再请求不存在的健康摘要接口
3. 居民首页不再直接访问 `/admin/**`
4. 公告可以正常展示，且来源于公共接口
5. Hero 顶部问候和敬老版按钮在移动端更清晰、更易点按
6. Hero 副标题能体现预约、消息、公告等动态信息，而不是单纯功能宣传语

---

## 8. 给执行同学的简短结论

这次不是“修一个报错”这么简单，而是顺手把居民首页的职责收清楚：

- 首页不再承担临时拼接的健康摘要接口
- 公告通过公共接口提供
- 首页顶部区域从“宣传语”改成“真实提醒入口”

这样改完之后，居民端首页会更像一个真正的服务入口页，结构也更稳。
