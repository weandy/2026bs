# 前端功能模块代码分析总览（按三类角色划分）

## 1. 文档目的

本文档不是基于需求文档做抽象归纳，而是基于当前前端代码实际实现进行梳理。

分析重点有三层：

1. 当前前端真正对外展示了哪些模块。
2. 每个模块在页面层面到底能做什么。
3. 代码里还存在哪些“已实现但未挂到主路由/主菜单”的潜在模块。

本次分析主要依据以下代码入口：

- `frontend/src/router/index.js`
- `frontend/src/components/layout/ResidentLayout.vue`
- `frontend/src/components/layout/AdminLayout.vue`
- `frontend/src/views/**`
- `frontend/src/stores/user.js`
- `frontend/src/utils/request.js`

---

## 2. 前端整体结构结论

### 2.1 技术栈与页面组织方式

当前前端采用以下技术组合：

- Vue 3 + Composition API
- Vue Router 4
- Pinia
- Element Plus
- Axios
- ECharts / vue-echarts
- Lucide 图标

代码依据：

- `frontend/package.json`
- `frontend/src/main.js`

这意味着系统前端是一个典型的单页应用，展示功能主要由三部分共同决定：

- 路由是否注册。
- 布局组件是否把该页面放入导航。
- 当前登录用户是否能通过守卫进入该路由。

### 2.2 三类角色的真实划分方式

系统前端不是靠单独的三个项目实现三端，而是同一个前端应用内做了三域分流：

- 居民端：`/resident/**`
- 医护端：`/medical/**`
- 管理端：`/admin/**`

代码依据：

- `frontend/src/router/index.js:26`
- `frontend/src/router/index.js:44`
- `frontend/src/router/index.js:60`

登录页会根据账号格式和后端返回的 `domain` / `role` 自动分流：

- 11 位手机号默认按居民登录。
- 非手机号按医护/管理员登录。
- 登录成功后自动跳转到居民首页、医护工作台或管理员仪表盘。

代码依据：

- `frontend/src/views/public/LoginPage.vue:113`
- `frontend/src/views/public/LoginPage.vue:132`
- `frontend/src/views/public/LoginPage.vue:138`
- `frontend/src/views/public/LoginPage.vue:152`
- `frontend/src/views/public/LoginPage.vue:154`
- `frontend/src/views/public/LoginPage.vue:156`

### 2.3 路由守卫与角色隔离机制

前端做了比较完整的域隔离：

- 居民不能访问医护和管理员路由。
- 管理域用户不能访问居民路由。
- 医护角色不能进入管理员专属路由。
- 无 token 或 domain 不明时会被打回登录页。

代码依据：

- `frontend/src/router/index.js:120`
- `frontend/src/router/index.js:124`
- `frontend/src/router/index.js:129`
- `frontend/src/router/index.js:135`
- `frontend/src/router/index.js:138`

### 2.4 “展示什么”最终由布局菜单控制

虽然 `views` 目录下存在不少页面文件，但真正稳定对外展示的主模块，是由两个布局组件里的导航配置决定的：

- 居民端菜单定义在 `ResidentLayout.vue`
- 医护端和管理员端菜单定义在 `AdminLayout.vue`

代码依据：

- `frontend/src/components/layout/ResidentLayout.vue:109`
- `frontend/src/components/layout/ResidentLayout.vue:115`
- `frontend/src/components/layout/ResidentLayout.vue:121`
- `frontend/src/components/layout/ResidentLayout.vue:130`
- `frontend/src/components/layout/AdminLayout.vue:126`
- `frontend/src/components/layout/AdminLayout.vue:139`

因此，分析“前端所展示的功能模块”时，应以“已注册路由 + 已进入导航”作为主判断标准，而不是只看 `views` 目录里有没有某个页面文件。

---

## 3. 居民端功能模块分析

居民端整体入口是 `ResidentLayout.vue`，导航被分成三组：

- 核心服务
- 健康管理
- 个人

同时还做了移动端底部导航与“更多”抽屉，因此它不只是 PC 页面，而是已经考虑了移动端主流程。

代码依据：

- `frontend/src/components/layout/ResidentLayout.vue:109`
- `frontend/src/components/layout/ResidentLayout.vue:115`
- `frontend/src/components/layout/ResidentLayout.vue:121`
- `frontend/src/components/layout/ResidentLayout.vue:130`

### 3.1 服务首页

页面文件：

- `frontend/src/views/resident/HomePage.vue`

当前页面实际承载的功能不是单纯欢迎页，而是居民端综合入口页，主要包含：

- 顶部 Hero 区，显示问候语、用户名、长辈模式开关。
- 今日提醒区，根据预约和未读消息数动态展示提醒。
- 我的预约卡片，展示最近 3 条预约。
- 消息通知卡片，展示最近 3 条消息与未读数。
- 社区公告卡片，拉取公共公告接口。
- 桌面端“近期就诊”模块。
- 移动端“快捷服务”九宫格。

代码依据：

- `frontend/src/views/resident/HomePage.vue:249`
- `frontend/src/views/resident/HomePage.vue:250`
- `frontend/src/views/resident/HomePage.vue:251`
- `frontend/src/views/resident/HomePage.vue:262`
- `frontend/src/views/resident/HomePage.vue:268`

这个首页的定位非常清晰：它不是独立业务页，而是“信息聚合页 + 各模块跳板”。

额外代码观察：

- 页面保留了“健康摘要”卡片结构，但 `healthSummary` 当前默认是 `null`，且注释明确写了“后端接口已取消”，所以这块 UI 目前大概率不会实际显示。

代码依据：

- `frontend/src/views/resident/HomePage.vue:108`
- `frontend/src/views/resident/HomePage.vue:164`
- `frontend/src/views/resident/HomePage.vue:258`

### 3.2 预约挂号

页面文件：

- `frontend/src/views/resident/appointment/AppointmentPage.vue`

这是居民端最完整的流程页之一，代码实现明显超过“简单预约表单”。

页面分成两个主模式：

- `新建预约`
- `我的预约`

#### 新建预约流程

代码里实现了四步式流程：

1. 选科室
2. 选日期、医生与时段
3. 填写就诊人信息
4. 提交成功回执

具体能力包括：

- 拉取开放预约科室列表。
- 支持选择日期后查看排班。
- 支持查看号源余量。
- 支持查看医生简介。
- 支持识别家庭医生并置顶显示。
- 支持为家庭成员代预约。
- 支持提交症状描述。

代码依据：

- `frontend/src/views/resident/appointment/AppointmentPage.vue:290`
- `frontend/src/views/resident/appointment/AppointmentPage.vue:292`
- `frontend/src/views/resident/appointment/AppointmentPage.vue:325`
- `frontend/src/views/resident/appointment/AppointmentPage.vue:374`
- `frontend/src/views/resident/appointment/AppointmentPage.vue:416`
- `frontend/src/views/resident/appointment/AppointmentPage.vue:468`

这说明居民预约模块已经不是“选个日期就提交”，而是做成了一个接近门诊挂号产品的完整前端闭环。

#### 我的预约

历史预约区支持：

- 按状态筛选。
- 查看预约编号、科室、医生、时间段。
- 取消待就诊预约。

这部分使预约模块同时覆盖了“创建”和“管理”两种使用场景。

### 3.3 就诊记录

页面文件：

- `frontend/src/views/resident/visit-record/VisitRecordPage.vue`

这个页面采用了明显的“主从结构”：

- 左侧记录列表
- 右侧详情面板
- 移动端则改为弹窗详情

页面支持的实际功能有：

- 按状态筛选就诊记录。
- 查看就诊基础信息。
- 查看主诉、现病史、体格检查、诊断、医嘱、复诊安排。
- 加载关联处方列表。
- 展开查看处方药品明细。

这意味着它不是一个“只看列表”的历史页，而是居民端医疗结果查看中心。

### 3.4 我的档案

页面文件：

- `frontend/src/views/resident/health-record/HealthRecordPage.vue`

该页是居民端健康档案总览页，当前代码呈现的内容包括：

- 慢病标签
- 过敏史
- 家族史
- 紧急联系人
- 最新生命体征
- 近期就诊记录
- 健康趋势折线图
- 导出健康摘要 PDF

代码依据：

- `frontend/src/views/resident/health-record/HealthRecordPage.vue:177`
- `frontend/src/views/resident/health-record/HealthRecordPage.vue:200`
- `frontend/src/views/resident/health-record/HealthRecordPage.vue:205`
- `frontend/src/views/resident/health-record/HealthRecordPage.vue:212`
- `frontend/src/views/resident/health-record/HealthRecordPage.vue:215`

从代码角度看，这页不是纯展示静态档案，而是把 `health-record`、`visit-record`、`vital/latest`、`vital trend` 几类数据做了聚合，因此更像居民健康总览中心。

### 3.5 疫苗接种

页面文件：

- `frontend/src/views/resident/vaccine/VaccinePage.vue`

当前分为三个标签页：

- 可接种疫苗
- 我的预约
- 接种记录

支持的能力有：

- 查看当前可预约的疫苗库存。
- 发起接种预约。
- 取消待接种预约。
- 通过时间线查看历史接种记录。
- 查看是否存在不良反应记录。

这说明居民侧接种模块已经覆盖了“预约前 - 预约中 - 已接种”三个阶段。

### 3.6 签约与随访

页面文件：

- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue`

这页承担两个业务面：

- 家庭医生签约
- 慢病随访查看

签约部分支持：

- 查看当前签约状态。
- 查看审核中、已签约、已驳回、已到期、已解除等状态。
- 申请签约。
- 申请解约。
- 选择科室后拉取可签约医生列表。

随访部分支持：

- 查看随访计划。
- 查看随访记录。
- 查看随访趋势图。

代码依据：

- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue:223`
- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue:230`
- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue:246`
- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue:253`
- `frontend/src/views/resident/followup/ResidentFollowUpPage.vue:277`

这表明居民端随访模块更偏“结果查看 + 发起申请”，而不是自行管理计划。

### 3.7 家庭成员

页面文件：

- `frontend/src/views/resident/family/FamilyPage.vue`

这是一个很有产品意味的模块，不只是“家庭成员通讯录”。

代码实现的核心能力包括：

- 新增、编辑、删除家庭成员。
- 展示成员关系、联系方式、身份证、备注等信息。
- 为不同关系自动预设代管权限。
- 在详情页里单独开关权限范围。
- 支持“代为预约挂号”。
- 支持“查看被代管人的健康摘要”。

代码依据：

- `frontend/src/views/resident/family/FamilyPage.vue:322`
- `frontend/src/views/resident/family/FamilyPage.vue:381`

从代码设计上看，这一页真正的业务核心不是成员资料，而是“代理权限管理”。

它把居民端从“个人自助系统”扩展成了“家庭代理式系统”。

### 3.8 个人中心

页面文件：

- `frontend/src/views/resident/ProfilePage.vue`

当前个人中心主要聚焦账号与基本资料管理，支持：

- 查看个人基础信息。
- 查看和维护紧急联系人。
- 修改密码。
- 修改手机号。
- 退出登录。

这说明居民端个人中心偏“账户安全 + 联系方式维护”，不承担复杂业务。

### 3.9 消息通知

页面文件：

- `frontend/src/views/resident/message/MessagePage.vue`

代码实现上，这页不是简单通知列表，而是支持：

- 分类筛选：预约、随访、接种、系统。
- 未读数展示。
- 单条已读。
- 全部标记已读。
- 桌面端主从布局查看详情。

因此它承担了居民端“所有提醒消息的统一收件箱”角色。

---

## 4. 医护端功能模块分析

医护端使用 `AdminLayout.vue`，但它不是管理员界面，而是医护与管理员共用壳层。

代码中医护导航按两组组织：

- 诊疗工作
- 公卫服务
- 账号

代码依据：

- `frontend/src/components/layout/AdminLayout.vue:126`

### 4.1 角色细分：医生与护士

医护端虽然整体归为一类，但代码里其实又做了医生/护士差异化展示：

- 工作台和处方页对 `NURSE` 设为不可见。
- 接种管理对 `DOCTOR` 设为不可见。

代码依据：

- `frontend/src/components/layout/AdminLayout.vue:128`
- `frontend/src/components/layout/AdminLayout.vue:130`
- `frontend/src/components/layout/AdminLayout.vue:134`

这说明前端设计意图是：

- 医生主负责接诊、诊断、处方。
- 护士主负责接种等执行型流程。

但要注意一个代码层面的实现细节：

- 路由守卫里使用的是小写角色值 `doctor` / `nurse`。
- 菜单显隐里比较的是大写 `DOCTOR` / `NURSE`。
- 后端登录返回 `roleCode`，具体值是否全大写，要以后端角色数据表为准。

因此，医护端细粒度显隐逻辑存在潜在不一致风险。

### 4.2 接诊工作台

页面文件：

- `frontend/src/views/medical/workbench/WorkbenchPage.vue`

这是医护端最核心的业务页面，实际上实现了一个“接诊闭环”。

代码实现的流程是：

- 拉取候诊队列。
- 对待叫号患者执行叫号。
- 对候诊中患者执行接诊。
- 接诊后显示患者摘要与高风险提醒。
- 录入体征、主诉、诊断、治疗方案。
- 完成接诊。
- 接诊完成后可继续开处方。
- 如果后端返回随访建议，则弹出建议创建随访计划。

代码依据：

- `frontend/src/views/medical/workbench/WorkbenchPage.vue:239`
- `frontend/src/views/medical/workbench/WorkbenchPage.vue:304`
- `frontend/src/views/medical/workbench/WorkbenchPage.vue:329`

这个页面非常关键，因为它把多个业务串成了一条链：

- 门诊候诊
- 就诊记录
- 健康摘要
- 处方
- 随访建议

从代码角度看，它就是系统最核心的医疗业务工作台。

### 4.3 居民档案管理

页面文件：

- `frontend/src/views/medical/record/RecordManagePage.vue`

支持的能力有：

- 根据姓名、手机号、居民 ID 搜索居民。
- 展示搜索结果列表。
- 进入单个居民健康档案。
- 编辑过敏史、既往病史、家族史、慢病标签、紧急联系人。

这页定位非常明确：

- 医护视角下的居民健康档案维护页。

### 4.4 签约与随访

页面文件：

- `frontend/src/views/medical/followup/FollowUpPage.vue`

这是医护端的“公卫服务工作台”，内容明显比居民侧更重。

它包括三块核心能力：

- 我的签约居民列表。
- 慢病随访计划管理。
- 公卫服务记录管理。

随访计划支持：

- 查看计划。
- 按慢病类型筛选。
- 查看今日到期计划。
- 新建计划。
- 停止计划。
- 查看随访记录。
- 添加随访记录。
- 查看趋势图。

公卫服务记录支持：

- 按服务类型筛选。
- 查看老年人体检、产前检查、儿童保健等记录。
- 新建公卫记录。

代码依据：

- `frontend/src/views/medical/followup/FollowUpPage.vue:246`
- `frontend/src/views/medical/followup/FollowUpPage.vue:253`
- `frontend/src/views/medical/followup/FollowUpPage.vue:303`
- `frontend/src/views/medical/followup/FollowUpPage.vue:328`
- `frontend/src/views/medical/followup/FollowUpPage.vue:361`
- `frontend/src/views/medical/followup/FollowUpPage.vue:371`

因此，医护侧的随访模块已经不只是“录一次随访结果”，而是包含签约、计划、执行、公卫服务四层。

### 4.5 接种管理

页面文件：

- `frontend/src/views/medical/vaccination/VaccinationManagePage.vue`

这个页面分为两个标签：

- 待接种
- 接种记录

支持的能力有：

- 查看待接种预约列表。
- 按疫苗和预约日期筛选待接种记录。
- 为预约登记实际接种。
- 查看历史接种记录。
- 为已接种记录补录不良反应。

这说明护士执行接种流程的核心页面已经完成。

### 4.6 处方记录

页面文件：

- `frontend/src/views/medical/prescription/PrescriptionPage.vue`

支持的能力有：

- 按就诊记录 ID 查询处方。
- 查看处方明细。
- 导出处方 PDF。
- 新建电子处方。
- 在创建处方时从药品库选择药品，并填写用法、剂量、频次、天数。

从代码角度看，这页既能“查历史”，也能“直接开方”，不是只读页面。

### 4.7 我的排班

页面文件：

- `frontend/src/views/medical/schedule/MySchedulePage.vue`

支持：

- 周视图查看个人排班。
- 查看每个排班日期下的时段号源余量。
- 查看自己的调班申请记录。
- 发起调班申请。

代码依据：

- `frontend/src/views/medical/schedule/MySchedulePage.vue:113`
- `frontend/src/views/medical/schedule/MySchedulePage.vue:120`
- `frontend/src/views/medical/schedule/MySchedulePage.vue:129`

这说明医护端不仅覆盖临床业务，也覆盖基础工作安排。

### 4.8 个人中心

页面文件：

- `frontend/src/views/medical/profile/MedicalProfilePage.vue`

当前功能偏职业资料与账号安全，支持：

- 编辑职称、从医年限、擅长领域、个人简介。
- 修改密码。
- 修改手机号。

代码依据：

- `frontend/src/views/medical/profile/MedicalProfilePage.vue:123`
- `frontend/src/views/medical/profile/MedicalProfilePage.vue:131`
- `frontend/src/views/medical/profile/MedicalProfilePage.vue:141`
- `frontend/src/views/medical/profile/MedicalProfilePage.vue:151`

这也解释了为什么居民预约页里能查看医生简介，因为这里就是数据来源维护页。

---

## 5. 管理员端功能模块分析

管理员端当前主路由只挂了 6 个模块。

代码依据：

- `frontend/src/router/index.js:65`
- `frontend/src/router/index.js:66`
- `frontend/src/router/index.js:67`
- `frontend/src/router/index.js:68`
- `frontend/src/router/index.js:69`
- `frontend/src/router/index.js:70`

因此，“当前管理员端真正对外展示的主模块”应认定为以下 6 个。

### 5.1 数据看板

页面文件：

- `frontend/src/views/admin/dashboard/DashboardPage.vue`

该页承担管理端首页作用，汇总的数据板块包括：

- 签约人数
- 随访计划
- 员工总数
- 系统公告
- 近 7 天就诊趋势图
- 科室就诊分布图
- 近期公告
- 签约统计
- 随访完成率
- 今日接诊概况
- 医生工作量排行
- 导出报表

代码依据：

- `frontend/src/views/admin/dashboard/DashboardPage.vue:145`
- `frontend/src/views/admin/dashboard/DashboardPage.vue:157`
- `frontend/src/views/admin/dashboard/DashboardPage.vue:165`
- `frontend/src/views/admin/dashboard/DashboardPage.vue:187`
- `frontend/src/views/admin/dashboard/DashboardPage.vue:235`

需要特别注意两个代码层面的真实情况：

- “今日接诊概况”当前是前端基于就诊趋势数据做估算，不是独立精确统计。
- “医生工作量排行”在接口不可用时会回退为演示数据。

这意味着管理员首页目前是“真实数据 + 估算数据 + 兜底演示数据”的混合体。

### 5.2 医护管理

页面文件：

- `frontend/src/views/admin/user/StaffManagePage.vue`

支持的能力有：

- 按姓名、工号、手机号搜索员工。
- 查看员工角色、科室、职称、状态。
- 新增员工。
- 编辑员工。
- 重置员工密码。
- 启用/禁用员工账号。

代码依据：

- `frontend/src/views/admin/user/StaffManagePage.vue:111`
- `frontend/src/views/admin/user/StaffManagePage.vue:132`

因此这页承担的是后台账号与人员组织管理。

### 5.3 排班管理

页面文件：

- `frontend/src/views/admin/schedule/ScheduleManagePage.vue`

支持的能力有：

- 按科室、日期范围筛选排班。
- 查看医生排班。
- 新增排班。
- 设置停诊。

从代码实现看，这页是中心化排班控制台。

### 5.4 科室管理

页面文件：

- `frontend/src/views/admin/dept/DeptManagePage.vue`

支持：

- 查看科室列表。
- 新增科室。
- 编辑科室。
- 删除科室。
- 配置是否开放预约。
- 配置排序与状态。

这页其实直接影响居民预约页能否看到某科室，因为预约页会读取科室开放配置。

### 5.5 居民管理

页面文件：

- `frontend/src/views/admin/resident/ResidentManagePage.vue`

功能相对聚焦：

- 搜索居民。
- 查看居民基础信息。
- 启用/停用居民账号。

它更像是后台用户治理页，而不是居民档案详情页。

### 5.6 签约管理

页面文件：

- `frontend/src/views/admin/contract/ContractManagePage.vue`

这是管理端对家庭医生签约业务的统一审核中心，支持：

- 查看待审核签约。
- 查看全部签约。
- 查看医生签约统计。
- 批准签约申请。
- 驳回签约申请。
- 对已签约记录执行解约。
- 手动创建签约。

这说明签约业务的最终审批权在管理员端，而不是医护端。

---

## 6. 代码中存在、但当前主前端未正式展示的模块

这一部分非常重要。

如果只看 `views` 目录，会误以为系统管理员模块非常多；但从路由和菜单看，其中相当一部分并未挂到当前正式导航里。

### 6.1 居民侧已写但未挂主路由的页面

#### 健康资讯

- 文件：`frontend/src/views/resident/article/ArticleListPage.vue`
- 能力：按分类查看文章、打开文章详情。
- 对应接口：`/resident/article`、`/resident/article/{id}`

这意味着代码里已经有居民健康宣教内容模块，但当前居民导航没有入口。

#### 候诊进度页

- 文件：`frontend/src/views/resident/appointment/QueueProgressPage.vue`
- 能力：查看当前叫号、个人排队位置、候诊列表、自动刷新。

这说明居民端曾实现过单独的候诊进度页，但当前路由中未挂载。

#### 旧版随访页

- 文件：`frontend/src/views/resident/follow-up/FollowUpPage.vue`
- 能力：查看随访计划、随访记录、自报指标。

这是一个旧版本页面，与当前使用的 `followup/ResidentFollowUpPage.vue` 并存，说明项目存在页面演进痕迹。

### 6.2 管理端已写但未挂主路由的页面

#### 疫苗库存管理

- 文件：`frontend/src/views/admin/vaccine/VaccineStockPage.vue`
- 能力：疫苗入库、预警看板、异动日志、库存维护。

#### 药品库存管理

- 文件：`frontend/src/views/admin/drug/DrugStockPage.vue`
- 能力：药品档案、库存总量、批次明细。

#### 药品出入库/效期看板

- 文件：`frontend/src/views/admin/drug/DrugLogPage.vue`
- 能力：库存日志、效期看板。

#### 数据字典管理

- 文件：`frontend/src/views/admin/dict/DictManagePage.vue`
- 能力：疾病码、药品码、疫苗码等字典树维护。

#### 系统配置

- 文件：`frontend/src/views/admin/config/SysConfigPage.vue`
- 能力：配置项在线编辑。

#### 审计日志

- 文件：`frontend/src/views/admin/audit/AuditLogPage.vue`
- 能力：按模块和结果查询审计日志。

#### 文章管理

- 文件：`frontend/src/views/admin/article/ArticleManagePage.vue`
- 能力：新建、编辑、发布健康资讯文章。

#### 调班审批

- 文件：`frontend/src/views/admin/schedule/ScheduleTransferPage.vue`
- 能力：审批医护调班申请。

结论很明确：

- 管理端代码储备的能力明显多于当前正式挂出的 6 个模块。
- 当前正式前端更像“第一版核心后台”，而不是“所有后台能力都已开放”。

### 6.3 公共展示页

#### 候诊公屏

- 文件：`frontend/src/views/public/PublicScreen.vue`
- 路由：`/screen/:deptCode`

这个页面不属于三类角色中的任一类登录态页面，但它是系统前台展示能力的一部分。

支持：

- 显示正在就诊。
- 显示候诊列表。
- WebSocket 实时刷新。
- WebSocket 断开后自动降级轮询。

因此从系统整体展示能力看，它应被归入“公共大屏模块”。

---

## 7. 从代码角度得出的系统前端能力画像

### 7.1 当前真正上线形态更像“3 端合一平台”

不是单纯的管理后台，也不是单纯的居民端 App，而是三类角色在同一前端里的组合：

- 居民侧偏自助服务、健康查看、家庭代理。
- 医护侧偏接诊执行、公卫随访、接种登记。
- 管理侧偏组织配置、审批和统计看板。

### 7.2 真正的核心业务闭环已经形成

从代码链路看，系统已经具备以下闭环：

- 居民预约挂号
- 医护叫号接诊
- 接诊后记录诊断
- 生成处方
- 形成就诊记录
- 需要时生成随访计划
- 居民侧查看记录与随访结果

这说明当前系统的重心不是单一 CRUD，而是围绕基层医疗服务流程在做整合。

### 7.3 居民端的“家庭代理能力”是前端特色模块

家庭成员页面不是附属页，而是功能创新点之一。

它把系统从“一个居民管理自己”升级为“一个居民管理家庭成员部分健康事务”，这在代码结构中已经非常明确。

### 7.4 管理端当前对外展示的是“核心后台”，不是“完整后台”

代码里管理员页面远多于当前路由真正开放的页面，这意味着：

- 系统后台能力还在逐步开放。
- 当前主导航更聚焦于关键业务闭环。

---

## 8. 关键代码观察与建议

### 8.1 首页健康摘要模块目前处于半保留状态

居民首页仍有健康摘要卡片 UI，但数据装载已被停掉，当前大概率不会显示。

建议：

- 如果不再使用，应移除 UI 壳层。
- 如果要恢复，应重新接入健康摘要接口或复用档案接口。

### 8.2 医护端菜单显隐与路由角色判断存在潜在大小写不一致

前端路由守卫用的是：

- `doctor`
- `nurse`

而菜单显隐使用的是：

- `DOCTOR`
- `NURSE`

如果后端实际返回值是小写，那么菜单细粒度权限将失效，导致：

- 护士看到本不该看到的工作台/处方菜单。
- 医生看到本不该看到的接种菜单。

建议统一角色枚举值。

### 8.3 管理端仪表盘中部分数据并非强实时真实统计

当前看板里存在：

- 前端估算值
- 演示回退值

这对演示阶段是可接受的，但如果作为正式运营看板，需要继续补全后端真实统计接口。

### 8.4 代码库已出现“并存页面”与“未挂载页面”

例如：

- `resident/followup/ResidentFollowUpPage.vue`
- `resident/follow-up/FollowUpPage.vue`

这类并存说明项目迭代较快，但也意味着：

- 后续需要统一页面版本。
- 需要清理不再使用的旧页面和死路由候选文件。

---

## 9. 最终结论

如果严格从当前代码出发，而不是从文档设想出发，那么可以把系统前端概括为：

- 居民端已经形成“首页聚合 + 预约挂号 + 健康档案 + 家庭代理 + 消息中心”的完整自助服务入口。
- 医护端已经形成“候诊接诊 + 档案维护 + 随访公卫 + 接种登记 + 处方管理 + 排班”的业务执行工作台。
- 管理员端当前对外开放的是“看板 + 医护 + 科室 + 排班 + 居民 + 签约”六大核心后台模块。

同时，代码里还储备了较多未挂载模块，尤其是：

- 药品管理
- 疫苗库存管理
- 审计日志
- 字典配置
- 系统配置
- 文章管理
- 调班审批

所以更准确地说，当前前端不是“功能少”，而是“核心功能已挂出，扩展能力仍在代码中待开放”。

