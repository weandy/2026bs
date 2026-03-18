# 家庭成员功能模块设计文档

> 本文档基于对现有代码的全面分析和 Brainstorming，描述"家庭成员"功能的完整业务设计与改造方案，
> 供 AI 辅助完成前后端实现与论文相关章节撰写。

---

## 0. 现状分析（What we have）

### 0.1 已实现的内容

| 层次 | 文件 | 状态 |
|------|------|------|
| 数据库 | `family_member` 表（含 `permission_scope` 列） | ✅ 已建 |
| 后端实体 | `FamilyMember.java` | ✅ 已建 |
| 后端接口 | `FamilyMemberController.java` | ✅ 已建 |
| 前端页面 | `FamilyPage.vue` | ⚠️ 仅基础CRUD，缺失核心功能 |

**后端接口清单（已存在）：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/resident/family` | 查询我的家庭成员列表 |
| POST | `/resident/family` | 添加家庭成员 |
| PUT | `/resident/family/{id}` | 编辑家庭成员 |
| DELETE | `/resident/family/{id}` | 软删除家庭成员 |
| GET | `/resident/family/{id}/proxy-view` | 代管查看（分级权限数据返回）|

### 0.2 现有前端的问题

当前 `FamilyPage.vue` 仅实现了最基础的 CRUD，存在以下缺失：

- ❌ 没有使用 `proxy-view` 接口（核心创新点功能闲置）
- ❌ 没有展示权限范围（permissionScope 字段从未展示）
- ❌ 没有"以此成员身份代操作"的入口
- ❌ 没有关联居民账号（`linkedResidentId`）的任何交互
- ❌ 没有 PC 端适配（仅 500px 固定宽度）
- ❌ 表单缺少性别、出生日期、备注字段

---

## 1. 背景与战略价值

家庭成员功能是本系统**创新点③「家庭代管分级权限」**的核心载体：

> **老人/儿童等不善于操作手机的群体，由家属代为管理日常医疗事务。**

其独特价值在于：
1. **代为预约**：代管人可以以被管理成员身份预约挂号
2. **查看健康**：根据授权权限查看被管理成员的健康档案、就诊记录
3. **随访代答**：代管人可以代为响应随访计划
4. **签约联动**：家庭医生签约时，代管人可以以被管理成员身份发起签约

### 1.1 核心角色关系

```
居民A（代管人）
    └── 家庭成员记录 ──> 居民B（被代管人，有/无账号）
           ├── relation：关系（配偶/父母/子女/祖父母/其他）
           ├── permissionScope：权限范围（CSV格式）
           └── linkedResidentId：关联到B的账号ID（若B有账号则非空）
```

---

## 2. Brainstorming：功能边界与设计决策

### 2.1 家庭成员的两种情况

| 情况 | 说明 | 处理方式 |
|------|------|---------|
| **有账号的家庭成员** | 被代管人自己也注册了平台账号 | `linkedResidentId` 有值，可代管其真实数据 |
| **无账号的家庭成员** | 只是一条信息记录（如未成年子女、老人） | `linkedResidentId` 为空，仅支持代预约 |

### 2.2 权限范围（permissionScope）设计

权限字段为 CSV 字符串，后端已实现按关系自动赋予默认权限：

| 权限项 | 含义 | 默认授予关系 |
|--------|------|------------|
| `basic` | 查看基本信息 | 所有关系 |
| `appointment` | 代为预约挂号 | 所有关系 |
| `health_record` | 查看健康档案（慢病、过敏史） | 配偶、父母、子女 |
| `vital` | 查看体征指标（血压、血糖） | 配偶、父母、子女 |
| `visit_record` | 查看就诊记录 | 配偶、子女 |
| `follow_up` | 代答随访问卷 | 子女（代管老人）|

**默认权限分配（后端已实现）：**
```
配偶 / 子女 → basic + appointment + health_record + visit_record + vital
父母         → basic + appointment + health_record + vital
其他         → basic + appointment
```

### 2.3 跨模块联动分析

| 模块 | 联动行为 | 优先级 |
|------|---------|--------|
| **预约挂号** | 增加"为家庭成员预约"选项 | P1 |
| **健康档案** | 可查看家庭成员的健康摘要（只读） | P2 |
| **就诊记录** | 切换查看家庭成员的就诊历史 | P3 |
| **签约随访** | 以被代管人身份发起家庭医生签约 | P3 |

### 2.4 页面设计方案（PC双栏 + 移动卡片）

```
PC端布局：
┌──────────────────────────────────────────────┐
│  家庭成员管理                      [+ 添加成员] │
├──────────────┬───────────────────────────────┤
│ 成员列表     │  张奶奶 · 祖父母               │
│              │  ─────────────────────────── │
│ [张奶奶]   > │  手机：138****8888            │
│ [祖父母]     │  身份证：310...****1234        │
│              │                               │
│ [小明]     > │  权限设置                     │
│ [子女]       │  [✓] 代为预约   [✓] 健康档案  │
│              │  [✓] 体征指标   [✓] 就诊记录  │
│              │  [ ] 随访代答                  │
│              │                               │
│              │  [代为预约] [查看健康] [编辑]  │
└──────────────┴───────────────────────────────┘

移动端：卡片列表 → 点击打开底部抽屉详情
```

---

## 3. 数据库设计

### 3.1 当前 family_member 表字段（已建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| owner_id | BIGINT | 代管人居民ID |
| member_name | VARCHAR(50) | 成员姓名 |
| relation | VARCHAR(20) | 关系枚举 |
| member_phone | VARCHAR(20) | 手机号 |
| id_card | VARCHAR(20) | 身份证号 |
| linked_resident_id | BIGINT | 关联平台账号ID（可空） |
| permission_scope | VARCHAR(200) | 权限范围CSV |
| status | TINYINT | 1正常 0已删除 |
| created_at | DATETIME | 创建时间 |

### 3.2 建议新增字段（执行ALTER补充）

```sql
ALTER TABLE family_member
    ADD COLUMN birth_date DATE        NULL COMMENT '出生日期',
    ADD COLUMN gender     TINYINT     NULL COMMENT '1男 2女',
    ADD COLUMN remark     VARCHAR(200) NULL COMMENT '备注（如：高血压长期随访患者）';
```

同步更新 `FamilyMember.java` 实体，添加以下字段：
```java
private LocalDate birthDate;
private Integer gender;
private String remark;
```

---

## 4. 后端接口设计

### 4.1 现有接口（已可用）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/resident/family` | 列表查询 |
| POST | `/resident/family` | 添加 |
| PUT | `/resident/family/{id}` | 编辑 |
| DELETE | `/resident/family/{id}` | 软删除 |
| GET | `/resident/family/{id}/proxy-view` | 代管健康数据（按权限裁剪返回）|

### 4.2 建议新增接口

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| PUT | `/resident/family/{id}/permission` | `{ "permissionScope": "basic,appointment,vital" }` | 单独更新权限（权限开关用）|
| POST | `/resident/family/{id}/link` | `{ "phone": "138xxxx" }` | 按手机号关联平台居民账号 |

### 4.3 proxy-view 接口返回结构（已实现）

```json
{
  "memberName": "张奶奶",
  "relation": "grandparent",
  "scopes": ["basic", "appointment", "health_record", "vital"],
  "healthRecord": {
    "chronicTags": "hypertension,diabetes",
    "allergyHistory": "青霉素"
  },
  "recentVitals": [
    { "systolicBp": 138, "diastolicBp": 88, "measureTime": "2026-03-01" }
  ],
  "recentVisits": []
}
```

> `recentVisits` 仅在有 `visit_record` 权限时返回，否则字段不出现。

---

## 5. 前端页面改造方案（FamilyPage.vue 重构）

### 5.1 引入的新组件结构

```
FamilyPage.vue（页面容器）
├── MemberList（左侧/移动端卡片列表）
│   └── MemberCard（单个成员卡片）
├── MemberDetail（右侧/移动端底部抽屉）
│   ├── 基本信息区（姓名、关系、手机、证件号脱敏）
│   ├── PermissionScope（权限开关组件）
│   └── QuickActions（代为预约、查看健康档案按钮）
└── MemberForm（添加/编辑弹窗）
```

### 5.2 添加/编辑表单字段（扩展后）

| 字段 | 必填 | 控件 |
|------|------|------|
| 姓名 | ✅ | 文本输入 |
| 与我的关系 | ✅ | 下拉（配偶/子女/父母/祖父母/兄弟姐妹/其他）|
| 性别 | ❌ | 单选按钮 |
| 出生日期 | ❌ | 日期选择器 |
| 手机号 | ❌ | 文本输入（用于后续关联账号）|
| 身份证号 | ❌ | 文本输入（展示时4+****+4脱敏）|
| 备注 | ❌ | 文本域 |
| 权限设置 | ✅ | 多选开关（根据关系自动预填）|

### 5.3 权限开关 UI 示意

```
权限设置（选择该成员可代为操作的内容）
─────────────────────────────────────────
[✓] 代为预约挂号
[✓] 查看健康档案（慢病标签、过敏史）
[✓] 查看体征指标（血压、血糖记录）
[ ] 查看就诊记录
[ ] 代答随访问卷

💡 提示：权限越大，建议关系越亲密（如子女可开启全部）
```

### 5.4 代管健康摘要展示（proxy-view 调用结果）

```
张奶奶的健康摘要  [仅供参考，数据来自其本人档案]
────────────────────────────────────
慢病标签   高血压  糖尿病
过敏史     青霉素
────────────────────────────────────
最近体征（最新3条）
  2026-03-01   收缩压 138  舒张压 88  空腹血糖 6.8
  2026-02-15   收缩压 142  舒张压 92  空腹血糖 7.2
────────────────────────────────────
最近就诊（需开启就诊记录权限）
  [权限未开启，请在权限设置中开启]
```

---

## 6. 预约挂号联动（P1改造）

在 `AppointmentPage.vue` 的预约表单中增加"为谁预约"分组：

```
为谁预约：
  ○ 本人（默认）
  ○ 家庭成员 [dropdown: 张奶奶(祖父母) | 小明(子女)]
```

**后端改造点：**
- `POST /resident/appointment` 请求体增加可选字段 `proxyMemberId`
- 预约记录存储时保存 `proxy_member_id`，展示时标注"代人预约"

**前端改造点：**
- `AppointmentPage.vue` 在表单顶部加载 `/resident/family` 列表
- 选择成员后，预约人名称自动切换为成员姓名

---

## 7. 论文关联说明（创新点③）

此模块直接体现**家庭代管分级权限**创新点，论文中可着重描述：

1. **轻量级权限模型**：用 `permission_scope` CSV 字段而非复杂RBAC，实现灵活的细粒度权限控制，技术实现简洁
2. **双态被代管人设计**：`linked_resident_id` 可空，优雅处理"有账号"和"无账号"两类被代管成员
3. **聚合代理接口**：`proxy-view` 一次 API 调用按权限裁剪返回异构数据（健康档案+体征+就诊），避免前端绕过权限多次请求
4. **关系驱动权限预填**：关系选择后自动推荐权限集，降低用户操作复杂度，体现以用户为中心的设计思路

---

## 8. 改造优先级

| 优先级 | 任务 | 涉及文件 | 估计工作量 |
|--------|------|---------|---------|
| P1 | FamilyPage.vue 重构为双栏布局 | `FamilyPage.vue` | 中 |
| P1 | 表单增加性别/出生日期/备注/权限开关 | `FamilyPage.vue` | 小 |
| P1 | FamilyMember 实体补充birthDate/gender/remark | `FamilyMember.java` + SQL | 小 |
| P2 | 代管健康摘要弹窗（proxy-view接口） | `FamilyPage.vue` | 小 |
| P2 | 预约挂号"为家庭成员预约"联动 | `AppointmentPage.vue` + 后端 | 中 |
| P3 | PUT /permission 权限单独更新接口 | `FamilyMemberController.java` | 小 |
| P3 | 就诊记录切换查看成员数据 | `VisitRecordPage.vue` | 大 |
